package com.pallet.backend.service;

import com.pallet.backend.dto.request.ThanhToanHoaDonRequest;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.ThanhToan;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhachHangRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.ThanhToanRepository;
import com.pallet.backend.repository.VaiTroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pallet_concurrency_test;MODE=PostgreSQL;"
                + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"
})
@ActiveProfiles("test")
class HoaDonThanhToanConcurrencyTest {

    private static final String TEN_DANG_NHAP = "nv-concurrency";

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private KhoRepository khoRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ThanhToanRepository thanhToanRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void haiThanhToanDongThoiChiTaoMotBanGhiThanhCong() throws Exception {
        Long hoaDonId = taoHoaDonChoThanhToan();

        // Giữ khóa hóa đơn để chắc chắn cả hai yêu cầu cùng chờ khóa trước khi một yêu cầu thắng.
        CountDownLatch daGiuKhoa = new CountDownLatch(1);
        CountDownLatch nhaKhoa = new CountDownLatch(1);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Thread giuKhoa = new Thread(() -> transactionTemplate.executeWithoutResult(status -> {
            hoaDonRepository.findForUpdate(hoaDonId).orElseThrow();
            daGiuKhoa.countDown();
            try {
                if (!nhaKhoa.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Hết thời gian giữ khóa hóa đơn");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }));
        giuKhoa.start();
        assertTrue(daGiuKhoa.await(5, TimeUnit.SECONDS), "Chưa giữ được khóa hóa đơn");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch sanSang = new CountDownLatch(2);
        CountDownLatch batDau = new CountDownLatch(1);
        List<Future<KetQua>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 2; i++) {
                futures.add(pool.submit(() -> {
                    sanSang.countDown();
                    if (!batDau.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Không thể bắt đầu thanh toán đồng thời");
                    }
                    ThanhToanHoaDonRequest request = new ThanhToanHoaDonRequest();
                    request.setPhuongThuc("chuyen_khoan");
                    try {
                        hoaDonService.thanhToan(hoaDonId, request, TEN_DANG_NHAP);
                        return new KetQua(true, null);
                    } catch (BusinessException e) {
                        return new KetQua(false, e);
                    }
                }));
            }

            assertTrue(sanSang.await(5, TimeUnit.SECONDS), "Hai luồng chưa sẵn sàng");
            batDau.countDown();
            // Cho cả hai luồng đi qua bước đọc nhân viên và chạm vào khóa hóa đơn.
            Thread.sleep(300);
            nhaKhoa.countDown();
            giuKhoa.join(5000);
            assertFalse(giuKhoa.isAlive(), "Luồng giữ khóa chưa kết thúc");

            List<KetQua> ketQua = new ArrayList<>();
            for (Future<KetQua> future : futures) {
                ketQua.add(future.get(20, TimeUnit.SECONDS));
            }

            assertEquals(1, ketQua.stream().filter(KetQua::thanhCong).count(),
                    "Chỉ đúng một yêu cầu được thanh toán thành công");

            KetQua thatBai = ketQua.stream()
                    .filter(k -> !k.thanhCong())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Yêu cầu còn lại phải nhận lỗi"));
            assertInstanceOf(BusinessException.class, thatBai.loi());
            assertEquals("Hóa đơn không ở trạng thái chờ thanh toán", thatBai.loi().getMessage());
        } finally {
            nhaKhoa.countDown();
            batDau.countDown();
            pool.shutdownNow();
            giuKhoa.join(5000);
        }

        List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDon_Id(hoaDonId);
        assertEquals(1, thanhToans.size(), "Chỉ được tạo một bản ghi thanh toán");
        assertEquals(1, thanhToans.stream().filter(t -> "thanh_cong".equals(t.getTrangThai())).count(),
                "Chỉ được có một bản ghi thanh toán thành công");

        HoaDon hoaDonSau = hoaDonRepository.findById(hoaDonId).orElseThrow();
        assertEquals("da_thanh_toan", hoaDonSau.getTrangThai());
    }

    private Long taoHoaDonChoThanhToan() {
        VaiTro vaiTro = vaiTroRepository.save(VaiTro.builder()
                .maVaiTro("GD")
                .tenVaiTro("Giám đốc")
                .capDuyet((short) 1)
                .build());

        Kho kho = khoRepository.save(Kho.builder()
                .maKho("KHO-CONCURRENCY")
                .tenKho("Kho kiểm thử đồng thời")
                .loaiDiaDiem("kho")
                .trangThai("hoat_dong")
                .build());

        NhanVien nhanVien = nhanVienRepository.save(NhanVien.builder()
                .vaiTro(vaiTro)
                .kho(kho)
                .maNv("NV-CONCURRENCY")
                .hoTen("Nhân viên kiểm thử đồng thời")
                .tenDangNhap(TEN_DANG_NHAP)
                .matKhauHash("khong-dung")
                .trangThai("dang_lam_viec")
                .build());

        KhachHang khachHang = khachHangRepository.save(KhachHang.builder()
                .maKh("KH-CONCURRENCY")
                .tenKh("Khách hàng kiểm thử đồng thời")
                .trangThai("dang_hop_tac")
                .createdAt(LocalDateTime.now())
                .build());

        HoaDon hoaDon = hoaDonRepository.save(HoaDon.builder()
                .khachHang(khachHang)
                .kho(kho)
                .nhanVienLap(nhanVien)
                .maHoaDon("HD-CONCURRENCY-1")
                .loaiGiaoDich("thu_ban_pallet")
                .ngayLap(LocalDateTime.now())
                .trangThai("cho_thanh_toan")
                .tongTienHang(new BigDecimal("100000"))
                .tongTienChietKhau(BigDecimal.ZERO)
                .tyLeGiamGiaTong(BigDecimal.ZERO)
                .tienGiamGiaTong(BigDecimal.ZERO)
                .tienTruocThue(new BigDecimal("100000"))
                .phiGiaoHang(BigDecimal.ZERO)
                .tyLeVat(BigDecimal.ZERO)
                .tienVat(BigDecimal.ZERO)
                .tongThanhToan(new BigDecimal("100000"))
                .daChotSo(false)
                .build());

        return hoaDon.getId();
    }

    private record KetQua(boolean thanhCong, BusinessException loi) {
    }
}
