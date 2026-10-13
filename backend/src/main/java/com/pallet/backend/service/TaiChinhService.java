package com.pallet.backend.service;

import com.pallet.backend.dto.request.ChiPhiVanHanhRequest;
import com.pallet.backend.dto.request.MucTieuDoanhThuRequest;
import com.pallet.backend.dto.response.BaoCaoDoanhThuResponse;
import com.pallet.backend.dto.response.BaoCaoLoiNhuanResponse;
import com.pallet.backend.dto.response.BaoCaoThueResponse;
import com.pallet.backend.dto.response.ChiPhiVanHanhResponse;
import com.pallet.backend.dto.response.MucTieuDoanhThuResponse;
import com.pallet.backend.entity.ChiPhiVanHanh;
import com.pallet.backend.entity.ChiTietHoaDon;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MucTieuDoanhThu;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiPhiVanHanhRepository;
import com.pallet.backend.repository.CompanyRepository;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.MucTieuDoanhThuRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.TaiChinhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaiChinhService {

    private static final BigDecimal TRAM = BigDecimal.valueOf(100);
    private static final String MUA_HANG = "mua_hang_dich_vu";
    private static final String[][] LOAI_CHI_PHI = {
            {"van_tai_logistics", "Vận tải & Logistics"},
            {"nhan_su", "Nhân sự kho bãi"},
            {"bao_tri_thiet_bi", "Bảo trì thiết bị"},
            {"dien_nang_luong", "Điện & Năng lượng"},
            {"khac", "Chi phí khác"},
    };

    private final TaiChinhRepository taiChinhRepository;
    private final ChiPhiVanHanhRepository chiPhiVanHanhRepository;
    private final MucTieuDoanhThuRepository mucTieuDoanhThuRepository;
    private final CompanyRepository companyRepository;
    private final HoaDonRepository hoaDonRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhoRepository khoRepository;

    // ======================= BÁO CÁO DOANH THU =======================

    @Transactional(readOnly = true)
    public BaoCaoDoanhThuResponse baoCaoDoanhThu(LocalDate tu, LocalDate den) {
        kiemTraKhoang(tu, den);
        LocalDateTime f = tu.atStartOfDay();
        LocalDateTime t = den.plusDays(1).atStartOfDay();
        LocalDate[] truoc = kyTruoc(tu, den);
        LocalDateTime fp = truoc[0].atStartOfDay();
        LocalDateTime tp = truoc[1].plusDays(1).atStartOfDay();

        BigDecimal doanhThu = nz(taiChinhRepository.tongDoanhThu(f, t));
        BigDecimal doanhThuTruoc = nz(taiChinhRepository.tongDoanhThu(fp, tp));
        long soHd = taiChinhRepository.demHoaDonThu(f, t);
        long soHdTruoc = taiChinhRepository.demHoaDonThu(fp, tp);

        // Biểu đồ 6 tháng gần nhất (tính đến tháng của ngày kết thúc, không vượt quá hôm nay)
        LocalDate homNay = LocalDate.now();
        YearMonth cuoi = YearMonth.from(den.isAfter(homNay) ? homNay : den);
        YearMonth dau = cuoi.minusMonths(5);
        Map<YearMonth, BigDecimal> theoThang = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) theoThang.put(dau.plusMonths(i), BigDecimal.ZERO);
        for (Object[] r : taiChinhRepository.doanhThuTheoNgay(
                dau.atDay(1).atStartOfDay(), cuoi.atEndOfMonth().plusDays(1).atStartOfDay())) {
            theoThang.merge(YearMonth.from(ngay(r[0])), (BigDecimal) r[1], BigDecimal::add);
        }
        List<BaoCaoDoanhThuResponse.DiemThang> bieuDo = theoThang.entrySet().stream()
                .map(e -> BaoCaoDoanhThuResponse.DiemThang.builder()
                        .nhan("Tháng " + e.getKey().getMonthValue())
                        .doanhThu(e.getValue()).build())
                .toList();

        // Cơ cấu pallet: top 5 + "Khác"
        List<Object[]> cc = taiChinhRepository.coCauMatHang(f, t);
        BigDecimal tongCc = cc.stream().map(r -> (BigDecimal) r[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BaoCaoDoanhThuResponse.CoCau> coCau = new ArrayList<>();
        BigDecimal khacTien = BigDecimal.ZERO;
        long khacSl = 0;
        String banChay = null;
        long slMax = -1;
        for (int i = 0; i < cc.size(); i++) {
            Object[] r = cc.get(i);
            BigDecimal tien = (BigDecimal) r[1];
            long sl = ((Number) r[2]).longValue();
            if (sl > slMax) {
                slMax = sl;
                banChay = (String) r[0];
            }
            if (i < 5) {
                coCau.add(BaoCaoDoanhThuResponse.CoCau.builder()
                        .ten((String) r[0]).soTien(tien).soLuong(sl).phanTram(tyTrong(tien, tongCc)).build());
            } else {
                khacTien = khacTien.add(tien);
                khacSl += sl;
            }
        }
        if (khacTien.signum() > 0) {
            coCau.add(BaoCaoDoanhThuResponse.CoCau.builder()
                    .ten("Khác").soTien(khacTien).soLuong(khacSl).phanTram(tyTrong(khacTien, tongCc)).build());
        }

        return BaoCaoDoanhThuResponse.builder()
                .doanhThu(doanhThu)
                .phanTramDoanhThu(pct(doanhThu, doanhThuTruoc))
                .soHoaDon(soHd)
                .phanTramSoHoaDon(pct(BigDecimal.valueOf(soHd), BigDecimal.valueOf(soHdTruoc)))
                .tonKhoPallet(nz(taiChinhRepository.tongTonKhoPallet()))
                .daGiuCho(nz(taiChinhRepository.tongGiuChoPallet()))
                .tongKhachHang(nz(taiChinhRepository.demKhachHangHopTac()))
                .khachHangMoi(nz(taiChinhRepository.demKhachHangMoi(f, t)))
                .mucTieu(timMucTieu(tu, den, doanhThu))
                .bieuDoThang(bieuDo)
                .coCauPallet(coCau)
                .sanPhamBanChayNhat(banChay)
                .giaoDichGanDay(layGiaoDichGanDay(f, t))
                .build();
    }

    private List<BaoCaoDoanhThuResponse.GiaoDich> layGiaoDichGanDay(LocalDateTime f, LocalDateTime t) {
        List<HoaDon> hds = taiChinhRepository.giaoDichGanDay(f, t, PageRequest.of(0, 100));
        if (hds.isEmpty()) return List.of();
        Map<Long, List<ChiTietHoaDon>> theoHd = taiChinhRepository
                .chiTietTheoHoaDon(hds.stream().map(HoaDon::getId).toList())
                .stream().collect(Collectors.groupingBy(c -> c.getHoaDon().getId()));

        return hds.stream().map(h -> {
            List<ChiTietHoaDon> ct = theoHd.getOrDefault(h.getId(), List.of());
            ChiTietHoaDon dauTien = ct.isEmpty() ? null : ct.get(0);
            return BaoCaoDoanhThuResponse.GiaoDich.builder()
                    .id(h.getId())
                    .maHoaDon(h.getMaHoaDon())
                    .tenKhachHang(h.getKhachHang().getTenKh())
                    .mstKhachHang(h.getKhachHang().getMst())
                    .ngayLap(h.getNgayLap())
                    .tenMatHang(dauTien != null ? dauTien.getMatHang().getTenMatHang() : null)
                    .soLuong(dauTien != null ? dauTien.getSoLuong() : null)
                    .soLoaiKhac(Math.max(0, ct.size() - 1))
                    .donGia(dauTien != null ? dauTien.getDonGia() : null)
                    .tongThanhToan(h.getTongThanhToan())
                    .trangThai(h.getTrangThai())
                    .quaHan("cho_thanh_toan".equals(h.getTrangThai())
                            && h.getHanThanhToan() != null && h.getHanThanhToan().isBefore(LocalDate.now()))
                    .build();
        }).toList();
    }

    // ======================= BÁO CÁO THUẾ =======================

    @Transactional(readOnly = true)
    public BaoCaoThueResponse baoCaoThue(LocalDate tu, LocalDate den) {
        kiemTraKhoang(tu, den);
        List<HoaDon> hds = taiChinhRepository.hoaDonThueTheoKy(tu.atStartOfDay(), den.plusDays(1).atStartOfDay());

        BigDecimal tongDt = BigDecimal.ZERO;
        BigDecimal tongThue = BigDecimal.ZERO;
        List<BaoCaoThueResponse.Dong> dong = new ArrayList<>();
        for (HoaDon h : hds) {
            tongDt = tongDt.add(h.getTienTruocThue());
            tongThue = tongThue.add(h.getTienVat());
            dong.add(BaoCaoThueResponse.Dong.builder()
                    .id(h.getId())
                    .maHoaDon(h.getMaHoaDon())
                    .ngayLap(h.getNgayLap())
                    .tenKhachHang(h.getKhachHang().getTenKh())
                    .doanhThuChuaThue(h.getTienTruocThue())
                    .tyLeVat(h.getTyLeVat())
                    .tienVat(h.getTienVat())
                    .trangThai(h.getTrangThai())
                    .build());
        }

        BigDecimal tyLeMacDinh = companyRepository.findAll().stream().findFirst()
                .map(c -> c.getTyLeVatMacDinh().multiply(TRAM).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.valueOf(8));

        return BaoCaoThueResponse.builder()
                .tongDoanhThuChuaThue(tongDt)
                .tongThue(tongThue)
                .tyLeVatMacDinh(tyLeMacDinh)
                .chiTiet(dong)
                .build();
    }

    // ======================= BÁO CÁO LỢI NHUẬN =======================

    @Transactional(readOnly = true)
    public BaoCaoLoiNhuanResponse baoCaoLoiNhuan(LocalDate tu, LocalDate den) {
        kiemTraKhoang(tu, den);
        LocalDateTime f = tu.atStartOfDay();
        LocalDateTime t = den.plusDays(1).atStartOfDay();
        LocalDate[] truoc = kyTruoc(tu, den);
        LocalDateTime fp = truoc[0].atStartOfDay();
        LocalDateTime tp = truoc[1].plusDays(1).atStartOfDay();

        BigDecimal doanhThu = nz(taiChinhRepository.tongDoanhThu(f, t));
        BigDecimal chiHoaDon = nz(taiChinhRepository.tongChiHoaDon(f, t));
        BigDecimal chiVanHanh = nz(taiChinhRepository.tongChiVanHanh(tu, den));
        BigDecimal chiPhi = chiHoaDon.add(chiVanHanh);
        BigDecimal loiNhuan = doanhThu.subtract(chiPhi);

        BigDecimal doanhThuTruoc = nz(taiChinhRepository.tongDoanhThu(fp, tp));
        BigDecimal chiPhiTruoc = nz(taiChinhRepository.tongChiHoaDon(fp, tp))
                .add(nz(taiChinhRepository.tongChiVanHanh(truoc[0], truoc[1])));
        BigDecimal loiNhuanTruoc = doanhThuTruoc.subtract(chiPhiTruoc);

        Map<String, BigDecimal> theoLoai = new HashMap<>();
        for (Object[] r : taiChinhRepository.chiVanHanhTheoLoai(tu, den)) {
            theoLoai.put((String) r[0], (BigDecimal) r[1]);
        }
        List<BaoCaoLoiNhuanResponse.HangMuc> hangMuc = new ArrayList<>();
        for (String[] l : LOAI_CHI_PHI) {
            hangMuc.add(BaoCaoLoiNhuanResponse.HangMuc.builder()
                    .ma(l[0]).ten(l[1]).soTien(theoLoai.getOrDefault(l[0], BigDecimal.ZERO)).build());
        }
        hangMuc.add(BaoCaoLoiNhuanResponse.HangMuc.builder()
                .ma(MUA_HANG).ten("Mua hàng & dịch vụ (hóa đơn chi)").soTien(chiHoaDon).build());

        return BaoCaoLoiNhuanResponse.builder()
                .doanhThu(doanhThu)
                .chiPhi(chiPhi)
                .loiNhuan(loiNhuan)
                .phanTramDoanhThu(pct(doanhThu, doanhThuTruoc))
                .phanTramChiPhi(pct(chiPhi, chiPhiTruoc))
                .phanTramLoiNhuan(pct(loiNhuan, loiNhuanTruoc))
                .bieuDo(gomTheoKy(tu, den,
                        taiChinhRepository.doanhThuTheoNgay(f, t),
                        taiChinhRepository.chiHoaDonTheoNgay(f, t),
                        taiChinhRepository.chiVanHanhTheoNgay(tu, den)))
                .chiPhiTheoHangMuc(hangMuc)
                .build();
    }

    /** Gom doanh thu / chi phí vào các cột biểu đồ: ≤ 92 ngày theo tuần, dài hơn theo tháng. */
    private List<BaoCaoLoiNhuanResponse.DiemSoSanh> gomTheoKy(LocalDate tu, LocalDate den, List<Object[]> thu,
                                                              List<Object[]> chiHd, List<Object[]> chiVh) {
        long len = ChronoUnit.DAYS.between(tu, den) + 1;
        boolean theoTuan = len <= 92;
        YearMonth ymTu = YearMonth.from(tu);
        int n = theoTuan
                ? (int) Math.max(1, Math.min(13, Math.round(len / 7.0)))
                : (int) ChronoUnit.MONTHS.between(ymTu, YearMonth.from(den)) + 1;

        BigDecimal[] dt = new BigDecimal[n];
        BigDecimal[] chi = new BigDecimal[n];
        Arrays.fill(dt, BigDecimal.ZERO);
        Arrays.fill(chi, BigDecimal.ZERO);
        gom(thu, dt, tu, den, len, n, theoTuan, ymTu);
        gom(chiHd, chi, tu, den, len, n, theoTuan, ymTu);
        gom(chiVh, chi, tu, den, len, n, theoTuan, ymTu);

        List<BaoCaoLoiNhuanResponse.DiemSoSanh> kq = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String nhan;
            if (theoTuan) {
                nhan = "Tuần " + (i + 1);
            } else {
                YearMonth ym = ymTu.plusMonths(i);
                nhan = n > 12 ? "T" + ym.getMonthValue() + "/" + ym.getYear() : "Tháng " + ym.getMonthValue();
            }
            kq.add(BaoCaoLoiNhuanResponse.DiemSoSanh.builder().nhan(nhan).doanhThu(dt[i]).chiPhi(chi[i]).build());
        }
        return kq;
    }

    private void gom(List<Object[]> rows, BigDecimal[] acc, LocalDate tu, LocalDate den,
                     long len, int n, boolean theoTuan, YearMonth ymTu) {
        for (Object[] r : rows) {
            LocalDate d = ngay(r[0]);
            if (d.isBefore(tu) || d.isAfter(den)) continue;
            int i = theoTuan
                    ? (int) Math.min(n - 1, ChronoUnit.DAYS.between(tu, d) * n / len)
                    : (int) ChronoUnit.MONTHS.between(ymTu, YearMonth.from(d));
            acc[i] = acc[i].add((BigDecimal) r[1]);
        }
    }

    // ======================= CHI PHÍ VẬN HÀNH (CRUD) =======================

    @Transactional(readOnly = true)
    public List<ChiPhiVanHanhResponse> layChiPhi(LocalDate tu, LocalDate den) {
        kiemTraKhoang(tu, den);
        return taiChinhRepository.chiPhiTrongKy(tu, den).stream().map(this::toChiPhi).toList();
    }

    @Transactional
    public ChiPhiVanHanhResponse taoChiPhi(ChiPhiVanHanhRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        ChiPhiVanHanh cp = ChiPhiVanHanh.builder()
                .kho(xacDinhKho(req.getKhoId(), nv))
                .nguoiTao(nv)
                .maChiPhi(hoaDonRepository.layMaChungTu("CP", "CP-"))
                .loaiChiPhi(req.getLoaiChiPhi())
                .moTa(req.getMoTa())
                .soTien(req.getSoTien())
                .ky(req.getKy() == null || req.getKy().isBlank() ? "thang" : req.getKy())
                .ngayChi(req.getNgayChi())
                .ghiChu(req.getGhiChu())
                .build();
        return toChiPhi(chiPhiVanHanhRepository.save(cp));
    }

    @Transactional
    public ChiPhiVanHanhResponse suaChiPhi(Long id, ChiPhiVanHanhRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        ChiPhiVanHanh cp = chiPhiVanHanhRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Khoản chi phí", id));
        if (req.getKhoId() != null) cp.setKho(xacDinhKho(req.getKhoId(), nv));
        cp.setLoaiChiPhi(req.getLoaiChiPhi());
        cp.setMoTa(req.getMoTa());
        cp.setSoTien(req.getSoTien());
        if (req.getKy() != null && !req.getKy().isBlank()) cp.setKy(req.getKy());
        cp.setNgayChi(req.getNgayChi());
        cp.setGhiChu(req.getGhiChu());
        return toChiPhi(chiPhiVanHanhRepository.save(cp));
    }

    @Transactional
    public void xoaChiPhi(Long id) {
        ChiPhiVanHanh cp = chiPhiVanHanhRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Khoản chi phí", id));
        chiPhiVanHanhRepository.delete(cp);
    }

    private ChiPhiVanHanhResponse toChiPhi(ChiPhiVanHanh c) {
        return ChiPhiVanHanhResponse.builder()
                .id(c.getId())
                .maChiPhi(c.getMaChiPhi())
                .loaiChiPhi(c.getLoaiChiPhi())
                .moTa(c.getMoTa())
                .soTien(c.getSoTien())
                .ky(c.getKy())
                .ngayChi(c.getNgayChi())
                .ghiChu(c.getGhiChu())
                .khoId(c.getKho().getId())
                .tenKho(c.getKho().getTenKho())
                .tenNguoiTao(c.getNguoiTao() != null ? c.getNguoiTao().getHoTen() : null)
                .build();
    }

    // ======================= MỤC TIÊU DOANH THU =======================

    @Transactional(readOnly = true)
    public List<MucTieuDoanhThuResponse> layMucTieu(Integer nam) {
        return mucTieuDoanhThuRepository.findByKhoIsNullAndNamKyOrderByThangKyAsc(nam).stream()
                .map(this::toMucTieu).toList();
    }

    @Transactional
    public MucTieuDoanhThuResponse datMucTieu(MucTieuDoanhThuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        boolean thang = "thang".equals(req.getKy());
        if (thang && req.getThangKy() == null) {
            throw new BusinessException("Vui lòng chọn tháng cho mục tiêu theo tháng");
        }
        Short thangKy = thang ? req.getThangKy().shortValue() : null;

        // Đã có mục tiêu cùng kỳ thì cập nhật, chưa có thì tạo mới
        MucTieuDoanhThu mt = (thang
                ? mucTieuDoanhThuRepository.findFirstByKhoIsNullAndKyAndNamKyAndThangKy("thang", req.getNamKy(), thangKy)
                : mucTieuDoanhThuRepository.findFirstByKhoIsNullAndKyAndNamKy("nam", req.getNamKy()))
                .orElseGet(() -> MucTieuDoanhThu.builder()
                        .ky(req.getKy()).namKy(req.getNamKy()).thangKy(thangKy).build());

        mt.setSoTienMucTieu(req.getSoTienMucTieu());
        mt.setGhiChu(req.getGhiChu());
        mt.setNguoiTao(nv);
        return toMucTieu(mucTieuDoanhThuRepository.save(mt));
    }

    @Transactional
    public void xoaMucTieu(Long id) {
        MucTieuDoanhThu mt = mucTieuDoanhThuRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Mục tiêu doanh thu", id));
        mucTieuDoanhThuRepository.delete(mt);
    }

    private MucTieuDoanhThuResponse toMucTieu(MucTieuDoanhThu m) {
        LocalDate[] kho = khoangCuaMucTieu(m);
        BigDecimal thucTe = nz(taiChinhRepository.tongDoanhThu(
                kho[0].atStartOfDay(), kho[1].plusDays(1).atStartOfDay()));
        return MucTieuDoanhThuResponse.builder()
                .id(m.getId()).ky(m.getKy()).namKy(m.getNamKy()).thangKy(m.getThangKy())
                .soTienMucTieu(m.getSoTienMucTieu()).ghiChu(m.getGhiChu())
                .doanhThuThucTe(thucTe)
                .phanTramDat(tyTrong(thucTe, m.getSoTienMucTieu()))
                .build();
    }

    private LocalDate[] khoangCuaMucTieu(MucTieuDoanhThu m) {
        if ("thang".equals(m.getKy()) && m.getThangKy() != null) {
            LocalDate dau = LocalDate.of(m.getNamKy(), m.getThangKy(), 1);
            return new LocalDate[]{dau, dau.with(TemporalAdjusters.lastDayOfMonth())};
        }
        return new LocalDate[]{LocalDate.of(m.getNamKy(), 1, 1), LocalDate.of(m.getNamKy(), 12, 31)};
    }

    /** Chỉ tìm mục tiêu khi khung thời gian đúng 1 tháng hoặc đúng 1 năm. */
    private BaoCaoDoanhThuResponse.MucTieu timMucTieu(LocalDate tu, LocalDate den, BigDecimal doanhThu) {
        MucTieuDoanhThu mt = null;
        if (tu.getDayOfMonth() == 1 && den.equals(tu.with(TemporalAdjusters.lastDayOfMonth()))) {
            mt = mucTieuDoanhThuRepository.findFirstByKhoIsNullAndKyAndNamKyAndThangKy(
                    "thang", tu.getYear(), (short) tu.getMonthValue()).orElse(null);
        } else if (tu.getDayOfYear() == 1 && den.equals(tu.with(TemporalAdjusters.lastDayOfYear()))) {
            mt = mucTieuDoanhThuRepository.findFirstByKhoIsNullAndKyAndNamKy("nam", tu.getYear()).orElse(null);
        }
        if (mt == null) return null;
        return BaoCaoDoanhThuResponse.MucTieu.builder()
                .id(mt.getId()).ky(mt.getKy()).namKy(mt.getNamKy()).thangKy(mt.getThangKy())
                .soTien(mt.getSoTienMucTieu())
                .phanTramDat(tyTrong(doanhThu, mt.getSoTienMucTieu()))
                .build();
    }

    // ======================= HELPER =======================

    private void kiemTraKhoang(LocalDate tu, LocalDate den) {
        if (tu == null || den == null) throw new BusinessException("Thiếu khoảng thời gian");
        if (den.isBefore(tu)) throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        if (ChronoUnit.DAYS.between(tu, den) > 1830) throw new BusinessException("Khoảng thời gian tối đa 5 năm");
    }

    /** Kỳ liền trước: tháng→tháng trước, quý→quý trước, năm→năm trước, còn lại dời lùi cùng số ngày. */
    private LocalDate[] kyTruoc(LocalDate tu, LocalDate den) {
        if (tu.getDayOfMonth() == 1 && den.equals(tu.with(TemporalAdjusters.lastDayOfMonth()))) {
            LocalDate t = tu.minusMonths(1);
            return new LocalDate[]{t, t.with(TemporalAdjusters.lastDayOfMonth())};
        }
        if (tu.getDayOfMonth() == 1 && tu.getMonthValue() % 3 == 1 && den.equals(tu.plusMonths(3).minusDays(1))) {
            LocalDate t = tu.minusMonths(3);
            return new LocalDate[]{t, tu.minusDays(1)};
        }
        if (tu.getDayOfYear() == 1 && den.equals(tu.with(TemporalAdjusters.lastDayOfYear()))) {
            return new LocalDate[]{tu.minusYears(1), tu.minusDays(1)};
        }
        long len = ChronoUnit.DAYS.between(tu, den) + 1;
        return new LocalDate[]{tu.minusDays(len), tu.minusDays(1)};
    }

    private Double pct(BigDecimal cur, BigDecimal prev) {
        if (prev.signum() == 0) return null;
        return cur.subtract(prev).multiply(TRAM).divide(prev.abs(), 1, RoundingMode.HALF_UP).doubleValue();
    }

    private Double tyTrong(BigDecimal part, BigDecimal tong) {
        if (tong == null || tong.signum() == 0) return 0d;
        return part.multiply(TRAM).divide(tong, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private LocalDate ngay(Object o) {
        return o instanceof LocalDateTime dt ? dt.toLocalDate() : (LocalDate) o;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Long nz(Long v) {
        return v == null ? 0L : v;
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
        throw new BusinessException("Tài khoản chưa được gán kho, vui lòng chọn kho");
    }
}