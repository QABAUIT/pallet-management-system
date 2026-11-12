package com.pallet.backend.service;

import com.pallet.backend.dto.request.CapNhatHoaDonRequest;
import com.pallet.backend.dto.request.TaoHoaDonRequest;
import com.pallet.backend.dto.request.ThanhToanHoaDonRequest;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.ThanhToan;
import com.pallet.backend.entity.TonKho;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.ChiTietHoaDonRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhachHangRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LichSuHoaDonRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.ThanhToanRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.service.impl.HoaDonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoaDonServiceImplTest {

    private static final String MSG_KHONG_DUOC_KHO =
            "Bạn chỉ được thao tác với dữ liệu của kho được gán cho mình";

    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Mock
    private ThanhToanRepository thanhToanRepository;
    @Mock
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private MatHangRepository matHangRepository;
    @Mock
    private TonKhoRepository tonKhoRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private KhoRepository khoRepository;
    @Mock
    private PhieuKhoRepository phieuKhoRepository;
    @Mock
    private ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    @Mock
    private ThongBaoTriggerService thongBaoTrigger;

    @Spy
    private PhanQuyenKho phanQuyenKho = new PhanQuyenKho();

    @InjectMocks
    private HoaDonServiceImpl hoaDonService;

    @Test
    void nvkhoKhongDuocThanhToanHoaDonKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));
        HoaDon hoaDon = hoaDon(10L, khoB, nhanVien("nguoi-lap", "NVKHO", khoB));

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hoaDonService.thanhToan(10L, new ThanhToanHoaDonRequest(), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(thanhToanRepository, lichSuHoaDonRepository, phieuKhoRepository);
    }

    @Test
    void gdDuocThanhToanHoaDonKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        HoaDon hoaDon = hoaDon(10L, khoB, nhanVien("nguoi-lap", "NVKHO", khoB));

        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));
        when(chiTietHoaDonRepository.findByHoaDon_Id(10L)).thenReturn(List.of());

        ThanhToanHoaDonRequest request = new ThanhToanHoaDonRequest();
        request.setPhuongThuc("chuyen_khoan");

        hoaDonService.thanhToan(10L, request, "gd");

        assertEquals("da_thanh_toan", hoaDon.getTrangThai());
        verify(thanhToanRepository).save(any(ThanhToan.class));
    }

    @Test
    void nvkhoKhongDuocCapNhatHoaDonKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));
        HoaDon hoaDon = hoaDon(10L, khoB, nhanVien("nguoi-lap", "NVKHO", khoB));

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hoaDonService.capNhat(10L, new CapNhatHoaDonRequest(), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(chiTietHoaDonRepository, lichSuHoaDonRepository);
    }

    @Test
    void nvkhoKhongDuocHuyHoaDonKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));
        HoaDon hoaDon = hoaDon(10L, khoB, nhanVien("nguoi-lap", "NVKHO", khoB));

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hoaDonService.huy(10L, "Lý do hủy", "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(chiTietHoaDonRepository, lichSuHoaDonRepository);
    }

    @Test
    void nvkhoKhongDuocTaoHoaDonChoKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));
        KhachHang khachHang = khachHang(3L);

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(khachHangRepository.findById(3L)).thenReturn(Optional.of(khachHang));
        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hoaDonService.taoMoi(taoHoaDonRequest(3L, 2L, 5L, 2), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(hoaDonRepository, chiTietHoaDonRepository, matHangRepository);
    }

    @Test
    void gdDuocTaoHoaDonChoKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        KhachHang khachHang = khachHang(3L);
        MatHang matHang = matHang(5L);
        TonKho tonKho = TonKho.builder()
                .id(1L).matHang(matHang).kho(khoB).soLuongTonKho(100).soLuongDaGiuCho(0).build();

        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(khachHangRepository.findById(3L)).thenReturn(Optional.of(khachHang));
        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));
        when(hoaDonRepository.layMaChungTu("HD", "HD-")).thenReturn("HD-1");
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(inv -> {
            HoaDon hoaDon = inv.getArgument(0);
            hoaDon.setId(1L);
            return hoaDon;
        });
        when(tonKhoRepository.findForUpdate(5L, 2L)).thenReturn(Optional.of(tonKho));

        TaoHoaDonResponse response = hoaDonService.taoMoi(taoHoaDonRequest(3L, 2L, 5L, 2), "gd");

        assertEquals("HD-1", response.maHoaDon());
        assertEquals(2, tonKho.getSoLuongDaGiuCho());
    }

    @Test
    void hoaDonKhoNullDungKhoNguoiLapDeKiemTra() {
        Kho khoA = kho(1L, "Kho A");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", khoA);
        HoaDon hoaDon = hoaDon(10L, null, nhanVien);

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        CapNhatHoaDonRequest request = new CapNhatHoaDonRequest();
        request.setGhiChu("Ghi chú mới");

        hoaDonService.capNhat(10L, request, "nv-a");

        assertEquals("Ghi chú mới", hoaDon.getGhiChu());
        verify(hoaDonRepository).save(hoaDon);
    }

    @Test
    void hoaDonKhoNullVaNguoiLapKhongCoKhoBiTuChoiVoiNvkho() {
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));
        HoaDon hoaDon = hoaDon(10L, null, nhanVien("nguoi-lap", "NVKHO", null));

        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> hoaDonService.capNhat(10L, new CapNhatHoaDonRequest(), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
    }

    @Test
    void hoaDonKhoNullVaNguoiLapKhongCoKhoGdDuocCapNhat() {
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        HoaDon hoaDon = hoaDon(10L, null, nhanVien("nguoi-lap", "NVKHO", null));

        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(hoaDonRepository.findForUpdate(10L)).thenReturn(Optional.of(hoaDon));

        CapNhatHoaDonRequest request = new CapNhatHoaDonRequest();
        request.setGhiChu("Ghi chú mới");

        hoaDonService.capNhat(10L, request, "gd");

        assertEquals("Ghi chú mới", hoaDon.getGhiChu());
    }

    private TaoHoaDonRequest taoHoaDonRequest(Long khachHangId, Long khoId, Long matHangId, int soLuong) {
        TaoHoaDonRequest.DongHang dongHang = new TaoHoaDonRequest.DongHang();
        dongHang.setMatHangId(matHangId);
        dongHang.setSoLuong(soLuong);

        TaoHoaDonRequest request = new TaoHoaDonRequest();
        request.setKhachHangId(khachHangId);
        request.setKhoId(khoId);
        request.setChiTiet(List.of(dongHang));
        return request;
    }

    private Kho kho(Long id, String tenKho) {
        return Kho.builder().id(id).tenKho(tenKho).build();
    }

    private NhanVien nhanVien(String tenDangNhap, String maVaiTro, Kho kho) {
        return NhanVien.builder()
                .id(99L)
                .tenDangNhap(tenDangNhap)
                .hoTen(tenDangNhap)
                .vaiTro(VaiTro.builder().maVaiTro(maVaiTro).build())
                .kho(kho)
                .build();
    }

    private KhachHang khachHang(Long id) {
        return KhachHang.builder()
                .id(id)
                .maKh("KH-" + id)
                .tenKh("Khách hàng " + id)
                .trangThai("dang_hop_tac")
                .build();
    }

    private MatHang matHang(Long id) {
        return MatHang.builder()
                .id(id)
                .maMatHang("MH-" + id)
                .tenMatHang("Pallet " + id)
                .loaiMatHang("linh_kien")
                .trangThai("dang_kinh_doanh")
                .donGiaBan(BigDecimal.valueOf(10))
                .build();
    }

    private HoaDon hoaDon(Long id, Kho kho, NhanVien nguoiLap) {
        return HoaDon.builder()
                .id(id)
                .maHoaDon("HD-" + id)
                .kho(kho)
                .nhanVienLap(nguoiLap)
                .trangThai("cho_thanh_toan")
                .daChotSo(false)
                .tongThanhToan(BigDecimal.valueOf(100))
                .build();
    }
}
