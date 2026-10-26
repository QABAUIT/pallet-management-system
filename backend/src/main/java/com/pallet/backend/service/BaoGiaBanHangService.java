package com.pallet.backend.service;

import com.pallet.backend.dto.request.ChuyenHoaDonRequest;
import com.pallet.backend.dto.request.TaoBaoGiaRequest;
import com.pallet.backend.dto.response.BaoGiaResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.entity.BaoGia;
import com.pallet.backend.entity.ChiTietBaoGia;
import com.pallet.backend.entity.ChiTietHoaDon;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LichSuHoaDon;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.BaoGiaRepository;
import com.pallet.backend.repository.ChiTietBaoGiaRepository;
import com.pallet.backend.repository.ChiTietHoaDonRepository;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhachHangRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LichSuHoaDonRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.TonKhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BaoGiaBanHangService {

    private static final BigDecimal VAT_MAC_DINH = BigDecimal.valueOf(8);
    private static final int NGAY_HIEU_LUC_MAC_DINH = 15;
    private static final int NGAY_HAN_THANH_TOAN_MAC_DINH = 15;

    private final BaoGiaRepository baoGiaRepository;
    private final ChiTietBaoGiaRepository chiTietBaoGiaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhoRepository khoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final NhatKyService nhatKyService;

    // ======================= ĐỌC =======================

    @Transactional
    public List<BaoGiaResponse> layDanhSach(String trangThai) {
        baoGiaRepository.danhDauHetHan();
        List<BaoGia> ds = (trangThai == null || trangThai.isBlank())
                ? baoGiaRepository.timTatCa() : baoGiaRepository.timTheoTrangThai(trangThai);
        return ds.stream().map(b -> toResponse(b, false)).toList();
    }

    @Transactional
    public BaoGiaResponse layChiTiet(Long id) {
        baoGiaRepository.danhDauHetHan();
        return toResponse(timBaoGia(id), true);
    }

    // ======================= TẠO / SỬA =======================

    @Transactional
    public BaoGiaResponse taoMoi(TaoBaoGiaRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        KhachHang kh = timKhachHang(req.getKhachHangId());
        int soNgay = req.getSoNgayHieuLuc() != null ? req.getSoNgayHieuLuc() : NGAY_HIEU_LUC_MAC_DINH;
        QuyTacBaoGia.kiemTraSoNgayHieuLuc(soNgay);
        List<ChiTietBaoGia> dong = dungDong(req);
        QuyTacBaoGia.Tong tong = tinhTong(dong, req);

        BaoGia b = baoGiaRepository.save(BaoGia.builder()
                .maBaoGia(hoaDonRepository.layMaChungTu("BG", "BG-"))
                .khachHang(kh).nguoiTao(nv).ngayTao(LocalDate.now())
                .hanHieuLuc(LocalDate.now().plusDays(soNgay)).trangThai("nhap")
                .tyLeVat(vat(req)).tongTien(tong.tongThanhToan()).tienGiamGiaTong(tong.tienGiam())
                .ghiChu(lamSach(req.getGhiChu()))
                .build());
        dong.forEach(c -> c.setBaoGia(b));
        chiTietBaoGiaRepository.saveAll(dong);

        nhatKyService.ghi(nv, "bao_gia", "tao_moi", "bao_gia", b.getId());
        return toResponse(b, true);
    }

    @Transactional
    public BaoGiaResponse capNhat(Long id, TaoBaoGiaRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        BaoGia b = khoa(id);
        if (!"nhap".equals(b.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được báo giá đang ở trạng thái nháp");
        }
        b.setKhachHang(timKhachHang(req.getKhachHangId()));
        int soNgay = req.getSoNgayHieuLuc() != null ? req.getSoNgayHieuLuc() : NGAY_HIEU_LUC_MAC_DINH;
        QuyTacBaoGia.kiemTraSoNgayHieuLuc(soNgay);
        List<ChiTietBaoGia> dong = dungDong(req);
        QuyTacBaoGia.Tong tong = tinhTong(dong, req);

        chiTietBaoGiaRepository.deleteAll(chiTietBaoGiaRepository.findByBaoGia_Id(b.getId()));
        chiTietBaoGiaRepository.flush();

        b.setHanHieuLuc(LocalDate.now().plusDays(soNgay));
        b.setTyLeVat(vat(req));
        b.setTongTien(tong.tongThanhToan());
        b.setTienGiamGiaTong(tong.tienGiam());
        b.setGhiChu(lamSach(req.getGhiChu()));
        b = baoGiaRepository.save(b);
        final BaoGia luu = b;
        dong.forEach(c -> c.setBaoGia(luu));
        chiTietBaoGiaRepository.saveAll(dong);

        nhatKyService.ghi(nv, "bao_gia", "cap_nhat", "bao_gia", b.getId());
        return toResponse(b, true);
    }

    // ======================= CHUYỂN TRẠNG THÁI =======================

    @Transactional
    public BaoGiaResponse gui(Long id, String tenDangNhap) {
        return chuyen(id, "da_gui", null, tenDangNhap);
    }

    @Transactional
    public BaoGiaResponse chapNhan(Long id, String tenDangNhap) {
        return chuyen(id, "da_chap_nhan", null, tenDangNhap);
    }

    @Transactional
    public BaoGiaResponse tuChoi(Long id, String lyDo, String tenDangNhap) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do khách từ chối báo giá");
        }
        return chuyen(id, "tu_choi", lyDo.trim(), tenDangNhap);
    }

    private BaoGiaResponse chuyen(Long id, String den, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        baoGiaRepository.danhDauHetHan();
        BaoGia b = khoa(id);
        if ("het_han".equals(b.getTrangThai())) {
            throw new BusinessException("Báo giá " + b.getMaBaoGia() + " đã hết hạn hiệu lực");
        }
        QuyTacBaoGia.kiemTraChuyenTrangThai(b.getTrangThai(), den);
        b.setTrangThai(den);
        if (lyDo != null) {
            String them = "Từ chối: " + lyDo;
            String gc = b.getGhiChu() == null ? them : b.getGhiChu() + ". " + them;
            b.setGhiChu(gc.length() > 255 ? gc.substring(0, 255) : gc);
        }
        b = baoGiaRepository.save(b);
        nhatKyService.ghi(nv, "bao_gia", den, "bao_gia", b.getId());
        return toResponse(b, true);
    }

    // ======================= CHUYỂN THÀNH HÓA ĐƠN =======================

    @Transactional
    public TaoHoaDonResponse chuyenThanhHoaDon(Long id, ChuyenHoaDonRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        BaoGia b = khoa(id);
        if (!"da_chap_nhan".equals(b.getTrangThai())) {
            throw new BusinessException("Chỉ chuyển thành hóa đơn khi khách đã chấp nhận báo giá");
        }
        if (b.getHoaDon() != null && !"da_huy".equals(b.getHoaDon().getTrangThai())) {
            throw new BusinessException("Báo giá " + b.getMaBaoGia() + " đã chuyển thành hóa đơn "
                    + b.getHoaDon().getMaHoaDon());
        }
        KhachHang kh = b.getKhachHang();
        if (!"dang_hop_tac".equals(kh.getTrangThai())) {
            throw new BusinessException("Khách hàng đã ngừng hợp tác");
        }
        Kho kho = xacDinhKho(req != null ? req.getKhoId() : null, nv);

        List<ChiTietBaoGia> lines = chiTietBaoGiaRepository.findByBaoGia_Id(b.getId()).stream()
                .sorted(Comparator.comparing(c -> c.getMatHang().getId())).toList();
        boolean coPallet = false;
        List<QuyTacBaoGia.Dong> dongTinh = new ArrayList<>();
        for (ChiTietBaoGia c : lines) {
            kiemTraMatHangBanDuoc(c.getMatHang());
            if ("pallet".equals(c.getMatHang().getLoaiMatHang())) coPallet = true;
            dongTinh.add(new QuyTacBaoGia.Dong(c.getSoLuong(), c.getDonGia(), c.getTyLeChietKhau()));
        }

        // Tổng tiền tính lại từ dòng với đúng tiền giảm tổng đã báo giá, để hóa đơn khớp báo giá
        BigDecimal tongHang = BigDecimal.ZERO, tongCk = BigDecimal.ZERO;
        for (QuyTacBaoGia.Dong d : dongTinh) {
            tongHang = tongHang.add(QuyTacBaoGia.thanhTienDong(d));
            tongCk = tongCk.add(QuyTacBaoGia.tienChietKhauDong(d));
        }
        BigDecimal tienGiam = b.getTienGiamGiaTong();
        BigDecimal truocThue = tongHang.subtract(tienGiam);
        BigDecimal tienVat = QuyTacDichVuThaoDo.tinhVat(truocThue, b.getTyLeVat());
        BigDecimal tyGiam = tongHang.signum() > 0
                ? tienGiam.multiply(BigDecimal.valueOf(100)).divide(tongHang, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Giữ chỗ tồn kho theo thứ tự id mặt hàng (tránh deadlock); thiếu hàng thì hủy cả giao dịch
        for (ChiTietBaoGia c : lines) {
            MatHang mh = c.getMatHang();
            var t = tonKhoRepository.findForUpdate(mh.getId(), kho.getId())
                    .orElseThrow(() -> new BusinessException(mh.getTenMatHang() + " chưa có tồn kho tại " + kho.getTenKho()));
            int khaDung = t.getSoLuongTonKho() - t.getSoLuongDaGiuCho();
            if (khaDung < c.getSoLuong()) {
                throw new BusinessException("Tồn kho khả dụng của " + mh.getTenMatHang() + " chỉ còn " + khaDung
                        + ", báo giá cần " + c.getSoLuong());
            }
            t.setSoLuongDaGiuCho(t.getSoLuongDaGiuCho() + c.getSoLuong());
            tonKhoRepository.save(t);
        }

        HoaDon hd = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon(hoaDonRepository.layMaChungTu("HD", "HD-"))
                .loaiGiaoDich(coPallet ? "thu_ban_pallet" : "thu_ban_linh_kien")
                .khachHang(kh).kho(kho).nhanVienLap(nv)
                .ngayLap(LocalDateTime.now()).trangThai("cho_thanh_toan")
                .tongTienHang(tongHang).tongTienChietKhau(tongCk)
                .tyLeGiamGiaTong(tyGiam).tienGiamGiaTong(tienGiam)
                .tienTruocThue(truocThue).phiGiaoHang(BigDecimal.ZERO)
                .tyLeVat(b.getTyLeVat()).tienVat(tienVat).tongThanhToan(truocThue.add(tienVat))
                .hanThanhToan(req != null && req.getHanThanhToan() != null
                        ? req.getHanThanhToan() : LocalDate.now().plusDays(NGAY_HAN_THANH_TOAN_MAC_DINH))
                .daChotSo(false).ghiChu("Từ báo giá " + b.getMaBaoGia())
                .build());
        for (ChiTietBaoGia c : lines) {
            chiTietHoaDonRepository.save(ChiTietHoaDon.builder()
                    .hoaDon(hd).matHang(c.getMatHang()).soLuong(c.getSoLuong())
                    .donGia(c.getDonGia()).tyLeChietKhau(c.getTyLeChietKhau()).build());
        }
        lichSuHoaDonRepository.save(LichSuHoaDon.builder()
                .hoaDon(hd).nguoiSua(nv).thoiGian(LocalDateTime.now())
                .hanhDong("tao_moi").lyDo("Lập hóa đơn từ báo giá " + b.getMaBaoGia()).build());

        b.setHoaDon(hd);
        baoGiaRepository.save(b);

        nhatKyService.ghi(nv, "bao_gia", "chuyen_hoa_don", "bao_gia", b.getId());
        return new TaoHoaDonResponse(hd.getId(), hd.getMaHoaDon(), hd.getTrangThai(),
                hd.getTongThanhToan(), hd.getTienThoiLai());
    }

    // ======================= HELPER =======================

    private void kiemTraMatHangBanDuoc(MatHang mh) {
        if (!"dang_kinh_doanh".equals(mh.getTrangThai())) {
            throw new BusinessException(mh.getTenMatHang() + " đã ngừng kinh doanh");
        }
        String loai = mh.getLoaiMatHang();
        if ("cho_tai_che".equals(loai) || "dich_vu".equals(loai) || "phe_lieu".equals(loai)) {
            throw new BusinessException(mh.getTenMatHang() + " không bán theo báo giá thông thường");
        }
    }

    private List<ChiTietBaoGia> dungDong(TaoBaoGiaRequest req) {
        Set<Long> daCo = new HashSet<>();
        List<ChiTietBaoGia> dong = new ArrayList<>();
        for (TaoBaoGiaRequest.Dong d : req.getChiTiet()) {
            if (!daCo.add(d.getMatHangId())) {
                throw new BusinessException("Mặt hàng bị trùng dòng trong báo giá, hãy gộp số lượng");
            }
            MatHang mh = matHangRepository.findById(d.getMatHangId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Mặt hàng", d.getMatHangId()));
            kiemTraMatHangBanDuoc(mh);
            dong.add(ChiTietBaoGia.builder().matHang(mh).soLuong(d.getSoLuong())
                    .donGia(d.getDonGia() != null ? d.getDonGia() : mh.getDonGiaBan())
                    .tyLeChietKhau(d.getTyLeChietKhau() != null ? d.getTyLeChietKhau() : BigDecimal.ZERO)
                    .build());
        }
        return dong;
    }

    private QuyTacBaoGia.Tong tinhTong(List<ChiTietBaoGia> dong, TaoBaoGiaRequest req) {
        List<QuyTacBaoGia.Dong> d = dong.stream()
                .map(c -> new QuyTacBaoGia.Dong(c.getSoLuong(), c.getDonGia(), c.getTyLeChietKhau())).toList();
        return QuyTacBaoGia.tinhTong(d,
                req.getTyLeGiamGiaTong() != null ? req.getTyLeGiamGiaTong() : BigDecimal.ZERO, vat(req));
    }

    private BigDecimal vat(TaoBaoGiaRequest req) {
        return req.getTyLeVat() != null ? req.getTyLeVat() : VAT_MAC_DINH;
    }

    private KhachHang timKhachHang(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Khách hàng", id));
        if (!"dang_hop_tac".equals(kh.getTrangThai())) {
            throw new BusinessException("Khách hàng đã ngừng hợp tác");
        }
        return kh;
    }

    private Kho xacDinhKho(Long khoId, NhanVien nv) {
        if (khoId != null) {
            return khoRepository.findById(khoId).orElseThrow(() -> ResourceNotFoundException.of("Kho", khoId));
        }
        if (nv.getKho() != null) return nv.getKho();
        throw new BusinessException("Tài khoản chưa được gán kho, vui lòng chọn kho xuất hàng");
    }

    private BaoGia timBaoGia(Long id) {
        return baoGiaRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Báo giá", id));
    }

    private BaoGia khoa(Long id) {
        return baoGiaRepository.findForUpdate(id).orElseThrow(() -> ResourceNotFoundException.of("Báo giá", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private BaoGiaResponse toResponse(BaoGia b, boolean chiTiet) {
        List<ChiTietBaoGia> lines = chiTietBaoGiaRepository.findByBaoGia_Id(b.getId()).stream()
                .sorted(Comparator.comparing(ChiTietBaoGia::getId)).toList();
        List<QuyTacBaoGia.Dong> dongTinh = lines.stream()
                .map(c -> new QuyTacBaoGia.Dong(c.getSoLuong(), c.getDonGia(), c.getTyLeChietKhau())).toList();
        BigDecimal tongHang = BigDecimal.ZERO;
        for (QuyTacBaoGia.Dong d : dongTinh) tongHang = tongHang.add(QuyTacBaoGia.thanhTienDong(d));
        BigDecimal truocThue = tongHang.subtract(b.getTienGiamGiaTong());

        BaoGiaResponse.BaoGiaResponseBuilder r = BaoGiaResponse.builder()
                .id(b.getId()).maBaoGia(b.getMaBaoGia()).trangThai(b.getTrangThai())
                .khachHangId(b.getKhachHang().getId()).tenKhachHang(b.getKhachHang().getTenKh())
                .tenNguoiTao(b.getNguoiTao().getHoTen())
                .ngayTao(b.getNgayTao()).hanHieuLuc(b.getHanHieuLuc()).tyLeVat(b.getTyLeVat())
                .tongTienHang(tongHang).tienGiamGiaTong(b.getTienGiamGiaTong())
                .tienVat(QuyTacDichVuThaoDo.tinhVat(truocThue, b.getTyLeVat()))
                .tongTien(b.getTongTien()).ghiChu(b.getGhiChu())
                .hoaDonId(b.getHoaDon() != null ? b.getHoaDon().getId() : null)
                .maHoaDon(b.getHoaDon() != null ? b.getHoaDon().getMaHoaDon() : null)
                .trangThaiHoaDon(b.getHoaDon() != null ? b.getHoaDon().getTrangThai() : null);
        if (chiTiet) {
            r.chiTiet(lines.stream().map(c -> BaoGiaResponse.Dong.builder()
                    .matHangId(c.getMatHang().getId()).maMatHang(c.getMatHang().getMaMatHang())
                    .tenMatHang(c.getMatHang().getTenMatHang()).soLuong(c.getSoLuong())
                    .donGia(c.getDonGia()).tyLeChietKhau(c.getTyLeChietKhau())
                    .thanhTien(QuyTacBaoGia.thanhTienDong(
                            new QuyTacBaoGia.Dong(c.getSoLuong(), c.getDonGia(), c.getTyLeChietKhau())))
                    .build()).toList());
        }
        return r.build();
    }
}