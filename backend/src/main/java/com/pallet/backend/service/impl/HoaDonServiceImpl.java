package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.CapNhatHoaDonRequest;
import com.pallet.backend.dto.request.TaoHoaDonRequest;
import com.pallet.backend.dto.request.ThanhToanHoaDonRequest;
import com.pallet.backend.dto.response.DanhMucBanHangResponse;
import com.pallet.backend.dto.response.HoaDonChiTietResponse;
import com.pallet.backend.dto.response.HoaDonResponse;
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
import com.pallet.backend.entity.TonKho;
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
import com.pallet.backend.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private static final String CHO_THANH_TOAN = "cho_thanh_toan";
    private static final String DA_THANH_TOAN = "da_thanh_toan";
    private static final String DA_HUY = "da_huy";
    private static final Set<String> PHUONG_THUC_HOP_LE = Set.of("tien_mat", "chuyen_khoan", "qr_code");
    private static final int SO_NGAY_HAN_MAC_DINH = 15;
    private static final BigDecimal VAT_MAC_DINH = BigDecimal.valueOf(8);
    private static final BigDecimal TRAM = BigDecimal.valueOf(100);

    private enum CheDoTon { GIU_CHO, XUAT, XUAT_TU_GIU_CHO, NHA_GIU_CHO }

    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final MatHangRepository matHangRepository;
    private final TonKhoRepository tonKhoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhoRepository khoRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;

    // ======================= ĐỌC =======================

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonResponse> layDanhSach(String trangThai) {
        List<HoaDon> ds = (trangThai == null || trangThai.isBlank())
                ? hoaDonRepository.timTatCa()
                : hoaDonRepository.timTheoTrangThai(trangThai);
        return ds.stream().map(this::toDong).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonChiTietResponse layChiTiet(Long id) {
        HoaDon hd = timHoaDon(id);
        List<ChiTietHoaDon> lines = chiTietHoaDonRepository.findByHoaDon_Id(id).stream()
                .sorted(Comparator.comparing(ChiTietHoaDon::getId)).toList();

        List<HoaDonChiTietResponse.Dong> dong = new ArrayList<>();
        int stt = 1;
        for (ChiTietHoaDon l : lines) {
            MatHang mh = l.getMatHang();
            BigDecimal gross = l.getDonGia().multiply(BigDecimal.valueOf(l.getSoLuong()));
            BigDecimal ck = l.getTienChietKhau() != null ? l.getTienChietKhau()
                    : gross.multiply(l.getTyLeChietKhau()).divide(TRAM, 2, RoundingMode.HALF_UP);
            BigDecimal thanhTien = l.getThanhTien() != null ? l.getThanhTien() : gross.subtract(ck);
            dong.add(HoaDonChiTietResponse.Dong.builder()
                    .stt(stt++)
                    .maMatHang(mh.getMaMatHang())
                    .tenMatHang(mh.getTenMatHang())
                    .moTa(mh.getMoTa())
                    .kichThuoc(kichThuoc(mh))
                    .soLuong(l.getSoLuong())
                    .donGia(l.getDonGia())
                    .tyLeChietKhau(l.getTyLeChietKhau())
                    .tienChietKhau(ck)
                    .thanhTien(thanhTien)
                    .build());
        }

        List<HoaDonChiTietResponse.LichSu> lichSu = lichSuHoaDonRepository.findByHoaDon_Id(id).stream()
                .sorted(Comparator.comparing(LichSuHoaDon::getThoiGian).reversed())
                .map(ls -> HoaDonChiTietResponse.LichSu.builder()
                        .thoiGian(ls.getThoiGian())
                        .hanhDong(ls.getHanhDong())
                        .nguoiSua(ls.getNguoiSua().getHoTen())
                        .truongThayDoi(ls.getTruongThayDoi())
                        .giaTriCu(ls.getGiaTriCu())
                        .giaTriMoi(ls.getGiaTriMoi())
                        .lyDo(ls.getLyDo())
                        .build())
                .toList();

        NhanVien nv = hd.getNhanVienLap();
        return HoaDonChiTietResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .loaiGiaoDich(hd.getLoaiGiaoDich())
                .laThu(hd.getLoaiGiaoDich().startsWith("thu_"))
                .trangThai(hd.getTrangThai())
                .quaHan(laQuaHan(hd))
                .daChotSo(Boolean.TRUE.equals(hd.getDaChotSo()))
                .ngayLap(hd.getNgayLap())
                .hanThanhToan(hd.getHanThanhToan())
                .ngayThanhToan(hd.getNgayThanhToan())
                .phuongThucThanhToan(hd.getPhuongThucThanhToan())
                .ghiChu(hd.getGhiChu())
                .tenKho(hd.getKho() != null ? hd.getKho().getTenKho() : null)
                .tongTienHang(hd.getTongTienHang())
                .tongTienChietKhau(hd.getTongTienChietKhau())
                .tyLeGiamGiaTong(hd.getTyLeGiamGiaTong())
                .tienGiamGiaTong(hd.getTienGiamGiaTong())
                .phiGiaoHang(hd.getPhiGiaoHang())
                .tienTruocThue(hd.getTienTruocThue())
                .tyLeVat(hd.getTyLeVat())
                .tienVat(hd.getTienVat())
                .tongThanhToan(hd.getTongThanhToan())
                .tienKhachDua(hd.getTienKhachDua())
                .tienThoiLai(hd.getTienThoiLai())
                .doiTac(toDoiTac(hd))
                .nhanVienLap(HoaDonChiTietResponse.NhanVienLap.builder()
                        .id(nv.getId())
                        .maNv(nv.getMaNv())
                        .hoTen(nv.getHoTen())
                        .chucVu(nv.getChucVu())
                        .tenBoPhan(nv.getBoPhan() != null ? nv.getBoPhan().getTenBoPhan() : null)
                        .build())
                .chiTiet(dong)
                .lichSu(lichSu)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucBanHangResponse layDanhMucBanHang(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = nv.getKho();

        Map<Long, Integer> ton = kho == null ? Map.of()
                : tonKhoRepository.findByKho_Id(kho.getId()).stream().collect(Collectors.toMap(
                        t -> t.getMatHang().getId(),
                        t -> t.getSoLuongTonKho() - t.getSoLuongDaGiuCho(),
                        Integer::sum));

        var khachHang = khachHangRepository.findByTrangThai("dang_hop_tac").stream()
                .map(k -> DanhMucBanHangResponse.KhachHangOption.builder()
                        .id(k.getId()).maKh(k.getMaKh()).tenKh(k.getTenKh())
                        .sdt(k.getSdt()).mst(k.getMst()).build())
                .toList();

        var matHang = matHangRepository.findByTrangThai("dang_kinh_doanh").stream()
                .filter(m -> !"cho_tai_che".equals(m.getLoaiMatHang()))
                .map(m -> DanhMucBanHangResponse.MatHangOption.builder()
                        .id(m.getId()).maMatHang(m.getMaMatHang()).tenMatHang(m.getTenMatHang())
                        .moTa(m.getMoTa()).kichThuoc(kichThuoc(m)).donGia(m.getDonGiaBan())
                        .tonKhaDung(Math.max(0, ton.getOrDefault(m.getId(), 0)))
                        .build())
                .toList();

        return DanhMucBanHangResponse.builder()
                .khoId(kho != null ? kho.getId() : null)
                .tenKho(kho != null ? kho.getTenKho() : null)
                .khachHang(khachHang)
                .matHang(matHang)
                .build();
    }

    // ======================= TẠO HÓA ĐƠN =======================

    @Override
    @Transactional
    public TaoHoaDonResponse taoMoi(TaoHoaDonRequest req, String tenDangNhap) {
        NhanVien nguoiLap = timNhanVien(tenDangNhap);
        KhachHang kh = khachHangRepository.findById(req.getKhachHangId())
                .orElseThrow(() -> ResourceNotFoundException.of("Khách hàng", req.getKhachHangId()));
        if (!"dang_hop_tac".equals(kh.getTrangThai())) {
            throw new BusinessException("Khách hàng đã ngừng hợp tác");
        }
        Kho kho = xacDinhKho(req.getKhoId(), nguoiLap);

        // Gộp các dòng trùng mặt hàng, sắp theo id để khóa tồn kho theo thứ tự cố định (tránh deadlock)
        Map<Long, TaoHoaDonRequest.DongHang> gop = new TreeMap<>();
        for (TaoHoaDonRequest.DongHang d : req.getChiTiet()) {
            gop.merge(d.getMatHangId(), d, (a, b) -> {
                a.setSoLuong(a.getSoLuong() + b.getSoLuong());
                return a;
            });
        }

        BigDecimal tongHang = BigDecimal.ZERO;
        BigDecimal tongCk = BigDecimal.ZERO;
        boolean coPallet = false;
        List<ChiTietHoaDon> dong = new ArrayList<>();

        for (TaoHoaDonRequest.DongHang d : gop.values()) {
            MatHang mh = matHangRepository.findById(d.getMatHangId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Mặt hàng", d.getMatHangId()));
            if (!"dang_kinh_doanh".equals(mh.getTrangThai())) {
                throw new BusinessException(mh.getTenMatHang() + " đã ngừng kinh doanh");
            }
            if ("cho_tai_che".equals(mh.getLoaiMatHang())) {
                throw new BusinessException(mh.getTenMatHang() + " đang chờ tái chế, không thể bán");
            }
            if ("pallet".equals(mh.getLoaiMatHang())) coPallet = true;

            BigDecimal rate = d.getTyLeChietKhau() == null ? BigDecimal.ZERO : d.getTyLeChietKhau();
            BigDecimal gross = mh.getDonGiaBan().multiply(BigDecimal.valueOf(d.getSoLuong()));
            BigDecimal ck = gross.multiply(rate).divide(TRAM, 2, RoundingMode.HALF_UP);
            tongHang = tongHang.add(gross.subtract(ck));
            tongCk = tongCk.add(ck);

            dong.add(ChiTietHoaDon.builder()
                    .matHang(mh).soLuong(d.getSoLuong())
                    .donGia(mh.getDonGiaBan()).tyLeChietKhau(rate).build());
        }

        BigDecimal tyGiam = nz(req.getTyLeGiamGiaTong());
        BigDecimal tienGiam = tongHang.multiply(tyGiam).divide(TRAM, 2, RoundingMode.HALF_UP);
        BigDecimal phi = nz(req.getPhiGiaoHang());
        BigDecimal truocThue = tongHang.subtract(tienGiam).add(phi);
        BigDecimal tyVat = req.getTyLeVat() != null ? req.getTyLeVat() : VAT_MAC_DINH;
        BigDecimal vat = truocThue.multiply(tyVat).divide(TRAM, 2, RoundingMode.HALF_UP);
        BigDecimal tong = truocThue.add(vat);

        LocalDateTime now = LocalDateTime.now();
        HoaDon hd = hoaDonRepository.save(HoaDon.builder()
                .maHoaDon(hoaDonRepository.layMaChungTu("HD", "HD-"))
                .loaiGiaoDich(coPallet ? "thu_ban_pallet" : "thu_ban_linh_kien")
                .khachHang(kh).kho(kho).nhanVienLap(nguoiLap)
                .ngayLap(now)
                .trangThai(CHO_THANH_TOAN)
                .tongTienHang(tongHang.setScale(2, RoundingMode.HALF_UP))
                .tongTienChietKhau(tongCk)
                .tyLeGiamGiaTong(tyGiam)
                .tienGiamGiaTong(tienGiam)
                .tienTruocThue(truocThue.setScale(2, RoundingMode.HALF_UP))
                .phiGiaoHang(phi)
                .tyLeVat(tyVat)
                .tienVat(vat)
                .tongThanhToan(tong.setScale(2, RoundingMode.HALF_UP))
                .hanThanhToan(req.getHanThanhToan() != null ? req.getHanThanhToan()
                        : LocalDate.now().plusDays(SO_NGAY_HAN_MAC_DINH))
                .daChotSo(false)
                .ghiChu(req.getGhiChu())
                .build());

        for (ChiTietHoaDon l : dong) l.setHoaDon(hd);
        chiTietHoaDonRepository.saveAll(dong);

        if (req.isThanhToanNgay()) {
            apDungTonKho(hd, dong, CheDoTon.XUAT);
            ghiNhanThanhToan(hd, req.getPhuongThucThanhToan(), req.getTienKhachDua(), null, nguoiLap);
            taoPhieuXuatKho(hd, dong, nguoiLap);
            ghiLichSu(hd, nguoiLap, "tao_moi", null, null, null, "Lập hóa đơn và thanh toán ngay");
        } else {
            apDungTonKho(hd, dong, CheDoTon.GIU_CHO);
            ghiLichSu(hd, nguoiLap, "tao_moi", null, null, null, "Lập hóa đơn, chờ thanh toán");
        }
        hd = hoaDonRepository.save(hd);

        return new TaoHoaDonResponse(hd.getId(), hd.getMaHoaDon(), hd.getTrangThai(),
                hd.getTongThanhToan(), hd.getTienThoiLai());
    }

    // ======================= THANH TOÁN / SỬA / HỦY =======================

    @Override
    @Transactional
    public void thanhToan(Long id, ThanhToanHoaDonRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        HoaDon hd = timHoaDon(id);
        kiemTraChoPhepSua(hd);
        if (!CHO_THANH_TOAN.equals(hd.getTrangThai())) {
            throw new BusinessException("Hóa đơn không ở trạng thái chờ thanh toán");
        }
        List<ChiTietHoaDon> lines = chiTietHoaDonRepository.findByHoaDon_Id(id);

        apDungTonKho(hd, lines, CheDoTon.XUAT_TU_GIU_CHO);
        ghiNhanThanhToan(hd, req.getPhuongThuc(), req.getTienKhachDua(), req.getMaGiaoDichNganHang(), nv);
        taoPhieuXuatKho(hd, lines, nv);
        hoaDonRepository.save(hd);
        ghiLichSu(hd, nv, "cap_nhat", "trang_thai", CHO_THANH_TOAN, DA_THANH_TOAN, "Ghi nhận thanh toán");
    }

    @Override
    @Transactional
    public void capNhat(Long id, CapNhatHoaDonRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        HoaDon hd = timHoaDon(id);
        kiemTraChoPhepSua(hd);

        if (req.getHanThanhToan() != null && !req.getHanThanhToan().equals(hd.getHanThanhToan())) {
            ghiLichSu(hd, nv, "cap_nhat", "han_thanh_toan", String.valueOf(hd.getHanThanhToan()),
                    String.valueOf(req.getHanThanhToan()), null);
            hd.setHanThanhToan(req.getHanThanhToan());
        }
        String ghiChuMoi = req.getGhiChu() == null || req.getGhiChu().isBlank() ? null : req.getGhiChu().trim();
        if (!java.util.Objects.equals(ghiChuMoi, hd.getGhiChu())) {
            ghiLichSu(hd, nv, "cap_nhat", "ghi_chu", hd.getGhiChu(), ghiChuMoi, null);
            hd.setGhiChu(ghiChuMoi);
        }
        hoaDonRepository.save(hd);
    }

    @Override
    @Transactional
    public void huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        HoaDon hd = timHoaDon(id);
        kiemTraChoPhepSua(hd);
        if (!CHO_THANH_TOAN.equals(hd.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được hóa đơn đang chờ thanh toán");
        }
        apDungTonKho(hd, chiTietHoaDonRepository.findByHoaDon_Id(id), CheDoTon.NHA_GIU_CHO);
        String cu = hd.getTrangThai();
        hd.setTrangThai(DA_HUY);
        hoaDonRepository.save(hd);
        ghiLichSu(hd, nv, "huy", "trang_thai", cu, DA_HUY, lyDo);
    }

    // ======================= HELPER =======================

    private void kiemTraChoPhepSua(HoaDon hd) {
        if (Boolean.TRUE.equals(hd.getDaChotSo())) {
            throw new BusinessException("Hóa đơn " + hd.getMaHoaDon() + " đã chốt sổ, không thể thay đổi");
        }
        if (DA_HUY.equals(hd.getTrangThai())) {
            throw new BusinessException("Hóa đơn đã bị hủy");
        }
    }

    private void ghiNhanThanhToan(HoaDon hd, String phuongThuc, BigDecimal tienKhachDua,
                                  String maGd, NhanVien nguoiXuLy) {
        if (phuongThuc == null || !PHUONG_THUC_HOP_LE.contains(phuongThuc)) {
            throw new BusinessException("Phương thức thanh toán không hợp lệ");
        }
        BigDecimal tong = hd.getTongThanhToan();
        if (tong.signum() <= 0) {
            throw new BusinessException("Hóa đơn có tổng tiền bằng 0, không thể thanh toán");
        }
        BigDecimal khachDua;
        BigDecimal thoiLai;
        if ("tien_mat".equals(phuongThuc)) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tong) < 0) {
                throw new BusinessException("Số tiền khách đưa chưa đủ để thanh toán");
            }
            khachDua = tienKhachDua;
            thoiLai = tienKhachDua.subtract(tong);
        } else {
            khachDua = tong;
            thoiLai = BigDecimal.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        hd.setTrangThai(DA_THANH_TOAN);
        hd.setPhuongThucThanhToan(phuongThuc);
        hd.setTienKhachDua(khachDua);
        hd.setTienThoiLai(thoiLai);
        hd.setNgayThanhToan(now);

        thanhToanRepository.save(ThanhToan.builder()
                .hoaDon(hd).nguoiXuLy(nguoiXuLy)
                .phuongThuc(phuongThuc).soTien(tong)
                .maGiaoDichNganHang(maGd)
                .trangThai("thanh_cong").tuDongXacNhan(false)
                .thoiGian(now).build());
    }

    private void apDungTonKho(HoaDon hd, List<ChiTietHoaDon> lines, CheDoTon che) {
        List<ChiTietHoaDon> sorted = lines.stream()
                .sorted(Comparator.comparing(l -> l.getMatHang().getId())).toList();
        for (ChiTietHoaDon l : sorted) {
            MatHang mh = l.getMatHang();
            TonKho t = tonKhoRepository.findForUpdate(mh.getId(), hd.getKho().getId())
                    .orElseThrow(() -> new BusinessException(
                            mh.getTenMatHang() + " chưa có tồn kho tại " + hd.getKho().getTenKho()));
            int q = l.getSoLuong();
            int khaDung = t.getSoLuongTonKho() - t.getSoLuongDaGiuCho();
            switch (che) {
                case GIU_CHO -> {
                    if (khaDung < q) throw thieuTon(mh, khaDung);
                    t.setSoLuongDaGiuCho(t.getSoLuongDaGiuCho() + q);
                }
                case XUAT -> {
                    if (khaDung < q) throw thieuTon(mh, khaDung);
                    t.setSoLuongTonKho(t.getSoLuongTonKho() - q);
                }
                case XUAT_TU_GIU_CHO -> {
                    if (t.getSoLuongTonKho() < q) throw thieuTon(mh, t.getSoLuongTonKho());
                    t.setSoLuongDaGiuCho(Math.max(0, t.getSoLuongDaGiuCho() - q));
                    t.setSoLuongTonKho(t.getSoLuongTonKho() - q);
                }
                case NHA_GIU_CHO -> t.setSoLuongDaGiuCho(Math.max(0, t.getSoLuongDaGiuCho() - q));
            }
            tonKhoRepository.save(t);
        }
    }

    private BusinessException thieuTon(MatHang mh, int con) {
        return new BusinessException("Tồn kho khả dụng của " + mh.getTenMatHang() + " chỉ còn " + con);
    }

    private void taoPhieuXuatKho(HoaDon hd, List<ChiTietHoaDon> lines, NhanVien nv) {
        PhieuKho pk = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(hoaDonRepository.layMaChungTu("PX", "PX-"))
                .loaiPhieu("xuat_kho")
                .kho(hd.getKho()).hoaDon(hd).nhanVienGiaoNhan(nv)
                .ngayGio(LocalDateTime.now())
                .trangThai("da_hoan_thanh")
                .ghiChu("Xuất kho theo hóa đơn " + hd.getMaHoaDon())
                .build());
        for (ChiTietHoaDon l : lines) {
            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(pk).matHang(l.getMatHang()).soLuong(l.getSoLuong()).build());
        }
    }

    private void ghiLichSu(HoaDon hd, NhanVien nv, String hanhDong, String truong,
                           String cu, String moi, String lyDo) {
        lichSuHoaDonRepository.save(LichSuHoaDon.builder()
                .hoaDon(hd).nguoiSua(nv).thoiGian(LocalDateTime.now())
                .hanhDong(hanhDong).truongThayDoi(truong)
                .giaTriCu(cu).giaTriMoi(moi).lyDo(lyDo).build());
    }

    private HoaDonResponse toDong(HoaDon h) {
        String ma = null, ten = null, mst = null, sdt = null;
        if (h.getKhachHang() != null) {
            ma = h.getKhachHang().getMaKh(); ten = h.getKhachHang().getTenKh();
            mst = h.getKhachHang().getMst(); sdt = h.getKhachHang().getSdt();
        } else if (h.getNcc() != null) {
            ma = h.getNcc().getMaNcc(); ten = h.getNcc().getTenNcc();
            mst = h.getNcc().getMst(); sdt = h.getNcc().getSdt();
        }
        boolean quaHan = laQuaHan(h);
        return HoaDonResponse.builder()
                .id(h.getId())
                .maHoaDon(h.getMaHoaDon())
                .loaiGiaoDich(h.getLoaiGiaoDich())
                .laThu(h.getLoaiGiaoDich().startsWith("thu_"))
                .ngayLap(h.getNgayLap())
                .trangThai(h.getTrangThai())
                .quaHan(quaHan)
                .soNgayQuaHan(quaHan ? ChronoUnit.DAYS.between(h.getHanThanhToan(), LocalDate.now()) : null)
                .hanThanhToan(h.getHanThanhToan())
                .tongThanhToan(h.getTongThanhToan())
                .phuongThucThanhToan(h.getPhuongThucThanhToan())
                .daChotSo(Boolean.TRUE.equals(h.getDaChotSo()))
                .maDoiTac(ma).tenDoiTac(ten).mstDoiTac(mst).sdtDoiTac(sdt)
                .tenNhanVienLap(h.getNhanVienLap().getHoTen())
                .build();
    }

    private HoaDonChiTietResponse.DoiTac toDoiTac(HoaDon h) {
        if (h.getKhachHang() != null) {
            KhachHang k = h.getKhachHang();
            return HoaDonChiTietResponse.DoiTac.builder().loai("khach_hang")
                    .ma(k.getMaKh()).ten(k.getTenKh()).mst(k.getMst())
                    .nguoiDaiDien(k.getNguoiDaiDien()).sdt(k.getSdt()).diaChi(k.getDiaChi()).build();
        }
        if (h.getNcc() != null) {
            var n = h.getNcc();
            return HoaDonChiTietResponse.DoiTac.builder().loai("nha_cung_cap")
                    .ma(n.getMaNcc()).ten(n.getTenNcc()).mst(n.getMst())
                    .nguoiDaiDien(n.getNguoiLienHe()).sdt(n.getSdt()).diaChi(n.getDiaChi()).build();
        }
        return null;
    }

    private boolean laQuaHan(HoaDon h) {
        return CHO_THANH_TOAN.equals(h.getTrangThai())
                && h.getHanThanhToan() != null
                && h.getHanThanhToan().isBefore(LocalDate.now());
    }

    private String kichThuoc(MatHang m) {
        if (m.getKichThuocDai() == null || m.getKichThuocRong() == null || m.getKichThuocCao() == null) return null;
        return m.getKichThuocDai() + " x " + m.getKichThuocRong() + " x " + m.getKichThuocCao();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private HoaDon timHoaDon(Long id) {
        return hoaDonRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Hóa đơn", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private Kho xacDinhKho(Long khoId, NhanVien nv) {
        if (khoId != null) {
            return khoRepository.findById(khoId).orElseThrow(() -> ResourceNotFoundException.of("Kho", khoId));
        }
        if (nv.getKho() != null) return nv.getKho();
        throw new BusinessException("Tài khoản chưa được gán kho, vui lòng chọn kho bán hàng");
    }
}