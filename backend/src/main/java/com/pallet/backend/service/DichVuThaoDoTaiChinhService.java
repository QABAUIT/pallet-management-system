package com.pallet.backend.service;

import com.pallet.backend.dto.request.LapHoaDonDichVuRequest;
import com.pallet.backend.dto.request.MuaLaiPheLieuRequest;
import com.pallet.backend.dto.response.PhieuDichVuThaoDoResponse;
import com.pallet.backend.entity.ChiTietHoaDon;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.LichSuHoaDon;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.MuaLaiPheLieuKhach;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuDichVuThaoDo;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.ThanhToan;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietHoaDonRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.LichSuHoaDonRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.MuaLaiPheLieuKhachRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuDichVuThaoDoRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.ThanhToanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** Phần tiền của dịch vụ tháo dỡ: hóa đơn thu phí dịch vụ và mua lại phế liệu của khách. */
@Service
@RequiredArgsConstructor
public class DichVuThaoDoTaiChinhService {

    private static final String MA_DICH_VU = "DV-TD-99";
    private static final BigDecimal VAT_DICH_VU_MAC_DINH = BigDecimal.valueOf(8);
    private static final Set<String> PHUONG_THUC_HOP_LE = Set.of("tien_mat", "chuyen_khoan", "qr_code");

    private final PhieuDichVuThaoDoRepository phieuRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MuaLaiPheLieuKhachRepository muaLaiRepository;
    private final TonKhoKhachNghiepVu tonKhoKhachNghiepVu;
    private final TonKhoNghiepVu tonKhoNghiepVu;
    private final PhanQuyenKho phanQuyenKho;
    private final NhatKyService nhatKyService;
    private final PhieuDichVuThaoDoService phieuDichVuService;

    // ======================= HÓA ĐƠN THU PHÍ DỊCH VỤ =======================

    @Transactional
    public PhieuDichVuThaoDoResponse lapHoaDon(Long phieuId, LapHoaDonDichVuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = khoaPhieu(phieuId);
        phanQuyenKho.kiemTra(nv, p.getKho());

        if (!"da_thao_do".equals(p.getTrangThai()) && !"da_tra_hang".equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ lập hóa đơn sau khi kết quả tháo dỡ đã được duyệt");
        }
        if (p.getHoaDon() != null && !"da_huy".equals(p.getHoaDon().getTrangThai())) {
            throw new BusinessException("Phiếu " + p.getMaPhieu() + " đã có hóa đơn " + p.getHoaDon().getMaHoaDon());
        }
        if (p.getPhiDichVu() == null || p.getPhiDichVu().signum() <= 0) {
            throw new BusinessException("Phí dịch vụ bằng 0, không cần lập hóa đơn");
        }

        MatHang dichVu = matHangRepository.findByMaMatHang(MA_DICH_VU)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng dịch vụ " + MA_DICH_VU));
        BigDecimal tyVat = req != null && req.getTyLeVat() != null ? req.getTyLeVat() : VAT_DICH_VU_MAC_DINH;
        BigDecimal truocThue = p.getPhiDichVu();
        BigDecimal vat = QuyTacDichVuThaoDo.tinhVat(truocThue, tyVat);

        String ghiChu = "Dịch vụ tháo dỡ theo phiếu " + p.getMaPhieu();
        if (req != null && req.getGhiChu() != null && !req.getGhiChu().isBlank()) {
            ghiChu += ". " + req.getGhiChu().trim();
        }

        HoaDon hd = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon(hoaDonRepository.layMaChungTu("HD", "HD-"))
                .loaiGiaoDich("thu_dich_vu_thao_do")
                .khachHang(p.getKhachHang()).kho(p.getKho()).nhanVienLap(nv)
                .ngayLap(LocalDateTime.now()).trangThai("cho_thanh_toan")
                .tongTienHang(truocThue).tongTienChietKhau(BigDecimal.ZERO)
                .tyLeGiamGiaTong(BigDecimal.ZERO).tienGiamGiaTong(BigDecimal.ZERO)
                .tienTruocThue(truocThue).phiGiaoHang(BigDecimal.ZERO)
                .tyLeVat(tyVat).tienVat(vat).tongThanhToan(truocThue.add(vat))
                .hanThanhToan(req != null && req.getHanThanhToan() != null
                        ? req.getHanThanhToan() : LocalDate.now().plusDays(15))
                .daChotSo(false).ghiChu(ghiChu)
                .build());

        chiTietHoaDonRepository.save(ChiTietHoaDon.builder()
                .hoaDon(hd).matHang(dichVu).soLuong(p.getSoPalletThaoDo())
                .donGia(p.getDonGiaDichVu()).tyLeChietKhau(BigDecimal.ZERO).build());

        ghiLichSu(hd, nv, "Lập hóa đơn dịch vụ tháo dỡ theo phiếu " + p.getMaPhieu());

        p.setHoaDon(hd);
        phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "lap_hoa_don", "phieu_dich_vu_thao_do", p.getId());
        return phieuDichVuService.layChiTiet(p.getId(), tenDangNhap);
    }

    // ======================= MUA LẠI PHẾ LIỆU CỦA KHÁCH =======================

    @Transactional
    public PhieuDichVuThaoDoResponse muaLaiPheLieu(Long phieuId, MuaLaiPheLieuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = khoaPhieu(phieuId);
        phanQuyenKho.kiemTra(nv, p.getKho());

        if (!"da_thao_do".equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ mua lại phế liệu sau khi kết quả tháo dỡ được duyệt và trước khi trả hàng");
        }
        if (!PHUONG_THUC_HOP_LE.contains(req.getPhuongThucThanhToan())) {
            throw new BusinessException("Phương thức thanh toán không hợp lệ");
        }
        int conLai = p.getKgPheLieu() - p.getKgPheLieuDaMua();
        QuyTacDichVuThaoDo.kiemTraMuaLai(conLai, req.getSoKg(), req.getDonGiaKg());

        MatHang pheLieu = matHangRepository.findByMaMatHang(QuyTacTaiChe.MA_PHE_LIEU)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng phế liệu " + QuyTacTaiChe.MA_PHE_LIEU));
        int kg = req.getSoKg();
        BigDecimal tong = req.getDonGiaKg().multiply(BigDecimal.valueOf(kg)).setScale(2, java.math.RoundingMode.HALF_UP);

        // 1. Phế liệu rời sổ khách, vào tồn kho công ty
        tonKhoKhachNghiepVu.tru(p.getKhachHang(), pheLieu, p.getKho(), kg);
        tonKhoNghiepVu.cong(pheLieu, p.getKho(), kg);

        PhieuKho nhap = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho").kho(p.getKho())
                .nhanVienGiaoNhan(nv).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh").laHeThong(true)
                .ghiChu("Mua lại " + kg + " kg phế liệu của khách " + p.getKhachHang().getTenKh()
                        + " theo phiếu " + p.getMaPhieu())
                .build());
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(nhap).matHang(pheLieu).soLuong(kg).ghiChu(p.getMaPhieu()).build());

        // 2. Hóa đơn chi, trả tiền ngay
        LocalDateTime now = LocalDateTime.now();
        String ghiChu = "Mua lại phế liệu của khách theo phiếu " + p.getMaPhieu();
        if (req.getGhiChu() != null && !req.getGhiChu().isBlank()) ghiChu += ". " + req.getGhiChu().trim();

        HoaDon hd = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon(hoaDonRepository.layMaChungTu("HD", "HD-"))
                .loaiGiaoDich("chi_mua_phe_lieu_khach")
                .khachHang(p.getKhachHang()).kho(p.getKho()).nhanVienLap(nv)
                .ngayLap(now).trangThai("da_thanh_toan")
                .tongTienHang(tong).tongTienChietKhau(BigDecimal.ZERO)
                .tyLeGiamGiaTong(BigDecimal.ZERO).tienGiamGiaTong(BigDecimal.ZERO)
                .tienTruocThue(tong).phiGiaoHang(BigDecimal.ZERO)
                .tyLeVat(BigDecimal.ZERO).tienVat(BigDecimal.ZERO).tongThanhToan(tong)
                .tienKhachDua(tong).tienThoiLai(BigDecimal.ZERO)
                .phuongThucThanhToan(req.getPhuongThucThanhToan()).ngayThanhToan(now)
                .daChotSo(false).ghiChu(ghiChu)
                .build());
        chiTietHoaDonRepository.save(ChiTietHoaDon.builder()
                .hoaDon(hd).matHang(pheLieu).soLuong(kg)
                .donGia(req.getDonGiaKg()).tyLeChietKhau(BigDecimal.ZERO).build());
        thanhToanRepository.save(ThanhToan.builder()
                .hoaDon(hd).nguoiXuLy(nv).phuongThuc(req.getPhuongThucThanhToan()).soTien(tong)
                .trangThai("thanh_cong").tuDongXacNhan(false).thoiGian(now).build());
        ghiLichSu(hd, nv, "Mua lại phế liệu của khách theo phiếu " + p.getMaPhieu());

        // 3. Ghi nhận trên phiếu dịch vụ
        muaLaiRepository.save(MuaLaiPheLieuKhach.builder()
                .phieuDichVu(p).hoaDon(hd).phieuKho(nhap).nhanVien(nv)
                .soKg(kg).donGiaKg(req.getDonGiaKg()).build());
        p.setKgPheLieuDaMua(p.getKgPheLieuDaMua() + kg);
        phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "mua_lai_phe_lieu", "phieu_dich_vu_thao_do", p.getId());
        return phieuDichVuService.layChiTiet(p.getId(), tenDangNhap);
    }

    // ======================= HELPER =======================

    private void ghiLichSu(HoaDon hd, NhanVien nv, String lyDo) {
        lichSuHoaDonRepository.save(LichSuHoaDon.builder()
                .hoaDon(hd).nguoiSua(nv).thoiGian(LocalDateTime.now())
                .hanhDong("tao_moi").lyDo(lyDo).build());
    }

    private PhieuDichVuThaoDo khoaPhieu(Long id) {
        return phieuRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu dịch vụ tháo dỡ", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }
}