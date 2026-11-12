package com.pallet.backend.service;

import com.pallet.backend.dto.request.BanPheLieuRequest;
import com.pallet.backend.dto.response.DanhMucBanHangResponse;
import com.pallet.backend.dto.response.DanhMucBanPheLieuResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.entity.ChiTietHoaDon;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LichSuHoaDon;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.ThanhToan;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** Bán phế liệu gỗ theo kg: hóa đơn thu_ban_phe_lieu (quy trình 10). */
@Service
@RequiredArgsConstructor
public class BanPheLieuService {

    private static final BigDecimal VAT_MAC_DINH = BigDecimal.valueOf(8);
    private static final Set<String> PHUONG_THUC_HOP_LE = Set.of("tien_mat", "chuyen_khoan", "qr_code");

    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final KhachHangRepository khachHangRepository;
    private final MatHangRepository matHangRepository;
    private final TonKhoRepository tonKhoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhoRepository khoRepository;
    private final PhanQuyenKho phanQuyenKho;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final NhatKyService nhatKyService;

    @Transactional(readOnly = true)
    public DanhMucBanPheLieuResponse layDanhMuc(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = nv.getKho();
        MatHang pl = layPheLieu();
        int ton = kho == null ? 0 : tonKhoRepository.findByMatHang_IdAndKho_Id(pl.getId(), kho.getId())
                .map(t -> Math.max(0, t.getSoLuongTonKho() - t.getSoLuongDaGiuCho())).orElse(0);
        var khach = khachHangRepository.findByTrangThai("dang_hop_tac").stream()
                .map(k -> DanhMucBanHangResponse.KhachHangOption.builder()
                        .id(k.getId()).maKh(k.getMaKh()).tenKh(k.getTenKh()).sdt(k.getSdt()).mst(k.getMst()).build())
                .toList();
        return DanhMucBanPheLieuResponse.builder()
                .khoId(kho != null ? kho.getId() : null).tenKho(kho != null ? kho.getTenKho() : null)
                .tonKhaDungKg(ton).donGiaKgGoiY(pl.getDonGiaBan()).khachHang(khach).build();
    }

    @Transactional
    public TaoHoaDonResponse ban(BanPheLieuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        KhachHang kh = khachHangRepository.findById(req.getKhachHangId())
                .orElseThrow(() -> ResourceNotFoundException.of("Khách hàng", req.getKhachHangId()));
        if (!"dang_hop_tac".equals(kh.getTrangThai())) {
            throw new BusinessException("Khách hàng đã ngừng hợp tác");
        }
        Kho kho = xacDinhKho(req.getKhoId(), nv);
        phanQuyenKho.kiemTra(nv, kho);
        MatHang pl = layPheLieu();

        BigDecimal giaKg = req.getDonGiaKg() != null ? req.getDonGiaKg() : pl.getDonGiaBan();
        if (giaKg == null || giaKg.signum() <= 0) {
            throw new BusinessException("Chưa có đơn giá phế liệu, hãy nhập đơn giá mỗi kg");
        }
        int kg = req.getSoKg();
        BigDecimal tyVat = req.getTyLeVat() != null ? req.getTyLeVat() : VAT_MAC_DINH;
        BigDecimal tongHang = giaKg.multiply(BigDecimal.valueOf(kg)).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal vat = QuyTacDichVuThaoDo.tinhVat(tongHang, tyVat);
        BigDecimal tong = tongHang.add(vat);

        var t = tonKhoRepository.findForUpdate(pl.getId(), kho.getId())
                .orElseThrow(() -> new BusinessException("Kho " + kho.getTenKho() + " chưa có phế liệu"));
        int khaDung = t.getSoLuongTonKho() - t.getSoLuongDaGiuCho();
        if (khaDung < kg) {
            throw new BusinessException("Phế liệu khả dụng tại " + kho.getTenKho() + " chỉ còn " + khaDung
                    + " kg, không đủ bán " + kg + " kg");
        }

        HoaDon hd = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon(hoaDonRepository.layMaChungTu("HD", "HD-"))
                .loaiGiaoDich("thu_ban_phe_lieu")
                .khachHang(kh).kho(kho).nhanVienLap(nv)
                .ngayLap(LocalDateTime.now()).trangThai("cho_thanh_toan")
                .tongTienHang(tongHang).tongTienChietKhau(BigDecimal.ZERO)
                .tyLeGiamGiaTong(BigDecimal.ZERO).tienGiamGiaTong(BigDecimal.ZERO)
                .tienTruocThue(tongHang).phiGiaoHang(BigDecimal.ZERO)
                .tyLeVat(tyVat).tienVat(vat).tongThanhToan(tong)
                .hanThanhToan(req.getHanThanhToan() != null ? req.getHanThanhToan() : LocalDate.now().plusDays(15))
                .daChotSo(false).ghiChu(req.getGhiChu())
                .build());
        ChiTietHoaDon dong = chiTietHoaDonRepository.save(ChiTietHoaDon.builder()
                .hoaDon(hd).matHang(pl).soLuong(kg).donGia(giaKg).tyLeChietKhau(BigDecimal.ZERO).build());

        if (req.isThanhToanNgay()) {
            t.setSoLuongTonKho(t.getSoLuongTonKho() - kg);
            tonKhoRepository.save(t);
            ghiNhanThanhToan(hd, req.getPhuongThucThanhToan(), req.getTienKhachDua(), nv);
            PhieuKho pk = phieuKhoRepository.save(PhieuKho.builder()
                    .maPhieu(hoaDonRepository.layMaChungTu("PX", "PX-"))
                    .loaiPhieu("xuat_kho").kho(kho).hoaDon(hd).nhanVienGiaoNhan(nv)
                    .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                    .ghiChu("Xuất bán phế liệu theo hóa đơn " + hd.getMaHoaDon()).build());
            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(pk).matHang(pl).soLuong(dong.getSoLuong()).build());
            ghiLichSu(hd, nv, "Bán phế liệu và thanh toán ngay");
        } else {
            t.setSoLuongDaGiuCho(t.getSoLuongDaGiuCho() + kg); // thanh toán sau sẽ xuất từ phần giữ chỗ
            tonKhoRepository.save(t);
            ghiLichSu(hd, nv, "Bán phế liệu, chờ thanh toán");
        }

        nhatKyService.ghi(nv, "ban_phe_lieu", "tao_moi", "hoa_don", hd.getId());
        return new TaoHoaDonResponse(hd.getId(), hd.getMaHoaDon(), hd.getTrangThai(),
                hd.getTongThanhToan(), hd.getTienThoiLai());
    }

    private void ghiNhanThanhToan(HoaDon hd, String phuongThuc, BigDecimal tienKhachDua, NhanVien nv) {
        if (phuongThuc == null || !PHUONG_THUC_HOP_LE.contains(phuongThuc)) {
            throw new BusinessException("Phương thức thanh toán không hợp lệ");
        }
        BigDecimal tong = hd.getTongThanhToan();
        BigDecimal khachDua = tong;
        BigDecimal thoiLai = BigDecimal.ZERO;
        if ("tien_mat".equals(phuongThuc)) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tong) < 0) {
                throw new BusinessException("Số tiền khách đưa chưa đủ để thanh toán");
            }
            khachDua = tienKhachDua;
            thoiLai = tienKhachDua.subtract(tong);
        }
        LocalDateTime now = LocalDateTime.now();
        hd.setTrangThai("da_thanh_toan");
        hd.setPhuongThucThanhToan(phuongThuc);
        hd.setTienKhachDua(khachDua);
        hd.setTienThoiLai(thoiLai);
        hd.setNgayThanhToan(now);
        hoaDonRepository.save(hd);
        thanhToanRepository.save(ThanhToan.builder()
                .hoaDon(hd).nguoiXuLy(nv).phuongThuc(phuongThuc).soTien(tong)
                .trangThai("thanh_cong").tuDongXacNhan(false).thoiGian(now).build());
    }

    private void ghiLichSu(HoaDon hd, NhanVien nv, String lyDo) {
        lichSuHoaDonRepository.save(LichSuHoaDon.builder()
                .hoaDon(hd).nguoiSua(nv).thoiGian(LocalDateTime.now())
                .hanhDong("tao_moi").lyDo(lyDo).build());
    }

    private MatHang layPheLieu() {
        return matHangRepository.findByMaMatHang(QuyTacTaiChe.MA_PHE_LIEU)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng phế liệu " + QuyTacTaiChe.MA_PHE_LIEU));
    }

    private Kho xacDinhKho(Long khoId, NhanVien nv) {
        if (khoId != null) {
            return khoRepository.findById(khoId).orElseThrow(() -> ResourceNotFoundException.of("Kho", khoId));
        }
        if (nv.getKho() != null) return nv.getKho();
        throw new BusinessException("Tài khoản chưa được gán kho, vui lòng chọn kho bán hàng");
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }
}