package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.NhanHangDonMuaRequest;
import com.pallet.backend.dto.request.TaoDonMuaRequest;
import com.pallet.backend.dto.response.DanhMucDonMuaResponse;
import com.pallet.backend.dto.response.DonMuaChiTietResponse;
import com.pallet.backend.dto.response.DonMuaResponse;
import com.pallet.backend.entity.ChiTietDonMua;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.DonMua;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LoHang;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhaCungCap;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietDonMuaRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.DonMuaRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LoHangRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhaCungCapRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.service.DonMuaService;
import com.pallet.backend.service.ThongBaoTriggerService;
import com.pallet.backend.service.TonKhoNghiepVu;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonMuaServiceImpl implements DonMuaService {

    private static final String NHAP = "nhap";
    private static final String DA_XAC_NHAN = "da_xac_nhan";
    private static final String NHAN_MOT_PHAN = "da_nhan_mot_phan";
    private static final String NHAN_DU = "da_nhan_du";
    private static final String DA_HUY = "da_huy";

    private final DonMuaRepository donMuaRepository;
    private final ChiTietDonMuaRepository chiTietDonMuaRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final LoHangRepository loHangRepository;
    private final NhatKyService nhatKyService;
    private final PhanQuyenKho phanQuyenKho;
    private final TonKhoNghiepVu tonKhoNghiepVu;
    private final ThongBaoTriggerService thongBaoTrigger;

    // ======================= ĐỌC =======================

    @Override
    @Transactional(readOnly = true)
    public List<DonMuaResponse> layDanhSach(String trangThai) {
        List<DonMua> ds = (trangThai == null || trangThai.isBlank())
                ? donMuaRepository.timTatCa()
                : donMuaRepository.timTheoTrangThai(trangThai);
        if (ds.isEmpty()) return List.of();

        Map<Long, List<ChiTietDonMua>> theoDon = chiTietDonMuaRepository
                .findByDonMua_IdIn(ds.stream().map(DonMua::getId).toList()).stream()
                .collect(Collectors.groupingBy(c -> c.getDonMua().getId()));

        return ds.stream()
                .map(d -> toDong(d, theoDon.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DonMuaChiTietResponse layChiTiet(Long id) {
        return dungChiTiet(timDonMua(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucDonMuaResponse layDanhMuc(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);

        var kho = khoRepository.findByTrangThai("hoat_dong").stream()
                .filter(k -> laQuanLy(nv) || nv.getKho() == null || k.getId().equals(nv.getKho().getId()))
                .map(k -> DanhMucDonMuaResponse.KhoOption.builder()
                        .id(k.getId()).maKho(k.getMaKho()).tenKho(k.getTenKho()).build())
                .toList();

        List<NhaCungCap> dsNcc = nhaCungCapRepository.findByTrangThai("dang_hop_tac");
        var ncc = dsNcc.stream()
                .map(n -> DanhMucDonMuaResponse.NccOption.builder()
                        .id(n.getId()).maNcc(n.getMaNcc()).tenNcc(n.getTenNcc())
                        .nhomHang(n.getNhomHang()).build())
                .toList();

        // Phế liệu (kg) là hàng thu về từ tháo dỡ, không mua qua đơn mua
        var matHang = matHangRepository.findByTrangThai("dang_kinh_doanh").stream()
                .filter(m -> !"phe_lieu".equals(m.getLoaiMatHang()))
                .map(m -> DanhMucDonMuaResponse.MatHangOption.builder()
                        .id(m.getId()).maMatHang(m.getMaMatHang()).tenMatHang(m.getTenMatHang())
                        .loaiMatHang(m.getLoaiMatHang()).chatLieu(m.getChatLieu())
                        .donViTinh(m.getDonViTinh()).build())
                .toList();

        return DanhMucDonMuaResponse.builder()
                .khoMacDinhId(nv.getKho() != null ? nv.getKho().getId() : null)
                .kho(kho).nhaCungCap(ncc).matHang(matHang)
                .build();
    }

    // ======================= TẠO / SỬA =======================

    @Override
    @Transactional
    public DonMuaChiTietResponse taoMoi(TaoDonMuaRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        NhaCungCap ncc = timNccHopTac(req.getNccId());
        Kho kho = xacDinhKho(req.getKhoId(), nv);
        kiemTraQuyenKho(nv, kho);
        kiemTraNgayDuKien(req.getNgayDuKienNhan(), LocalDate.now());

        List<ChiTietDonMua> dong = taoDong(req.getChiTiet());

        DonMua dm = DonMua.builder()
                .maDonMua(donMuaRepository.layMaChungTu("DM", "DM-"))
                .ncc(ncc).kho(kho).nhanVienLap(nv)
                .ngayDat(LocalDate.now())
                .ngayDuKienNhan(req.getNgayDuKienNhan())
                .trangThai(NHAP)
                .tongTien(tinhTong(dong))
                .ghiChu(lamSach(req.getGhiChu()))
                .build();
        dm = donMuaRepository.save(dm);

        for (ChiTietDonMua l : dong) l.setDonMua(dm);
        chiTietDonMuaRepository.saveAll(dong);

        ghiNhatKy(nv, "tao_moi", dm);
        return dungChiTiet(dm);
    }

    @Override
    @Transactional
    public DonMuaChiTietResponse capNhat(Long id, TaoDonMuaRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        DonMua dm = khoaDonMua(id);
        if (!NHAP.equals(dm.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được đơn mua đang ở trạng thái nháp");
        }
        kiemTraQuyenKho(nv, dm.getKho());

        NhaCungCap ncc = timNccHopTac(req.getNccId());
        Kho kho = xacDinhKho(req.getKhoId() != null ? req.getKhoId() : dm.getKho().getId(), nv);
        kiemTraQuyenKho(nv, kho);
        kiemTraNgayDuKien(req.getNgayDuKienNhan(), dm.getNgayDat());

        List<ChiTietDonMua> dongMoi = taoDong(req.getChiTiet());

        // Xóa dòng cũ và flush TRƯỚC khi thêm dòng mới (tránh vướng UNIQUE don_mua_id + mat_hang_id)
        List<ChiTietDonMua> dongCu = chiTietDonMuaRepository.findByDonMua_Id(id);
        chiTietDonMuaRepository.deleteAll(dongCu);
        chiTietDonMuaRepository.flush();

        dm.setNcc(ncc);
        dm.setKho(kho);
        dm.setNgayDuKienNhan(req.getNgayDuKienNhan());
        dm.setGhiChu(lamSach(req.getGhiChu()));
        dm.setTongTien(tinhTong(dongMoi));
        dm = donMuaRepository.save(dm);

        for (ChiTietDonMua l : dongMoi) l.setDonMua(dm);
        chiTietDonMuaRepository.saveAll(dongMoi);

        ghiNhatKy(nv, "cap_nhat", dm);
        return dungChiTiet(dm);
    }

    // ======================= XÁC NHẬN / HỦY =======================

    @Override
    @Transactional
    public DonMuaChiTietResponse xacNhan(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        DonMua dm = khoaDonMua(id);
        if (!NHAP.equals(dm.getTrangThai())) {
            throw new BusinessException("Chỉ xác nhận được đơn mua đang ở trạng thái nháp");
        }
        if (chiTietDonMuaRepository.findByDonMua_Id(id).isEmpty()) {
            throw new BusinessException("Đơn mua chưa có dòng hàng nào");
        }
        if (dm.getNcc() == null || !"dang_hop_tac".equals(dm.getNcc().getTrangThai())) {
            throw new BusinessException("Nhà cung cấp đã ngừng hợp tác, không thể xác nhận đơn");
        }
        dm.setTrangThai(DA_XAC_NHAN);
        dm.setNguoiXacNhan(nv);
        donMuaRepository.save(dm);
        ghiNhatKy(nv, "xac_nhan", dm);
        return dungChiTiet(dm);
    }

    @Override
    @Transactional
    public DonMuaChiTietResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy đơn mua");
        }
        DonMua dm = khoaDonMua(id);
        if (!NHAP.equals(dm.getTrangThai()) && !DA_XAC_NHAN.equals(dm.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được đơn nháp hoặc đơn đã xác nhận nhưng chưa nhận hàng");
        }
        boolean daNhan = chiTietDonMuaRepository.findByDonMua_Id(id).stream()
                .anyMatch(l -> l.getSoLuongDaNhan() > 0);
        if (daNhan) {
            throw new BusinessException("Đơn đã nhận hàng, không thể hủy");
        }
        dm.setTrangThai(DA_HUY);
        dm.setLyDoHuy(lyDo.trim().length() > 255 ? lyDo.trim().substring(0, 255) : lyDo.trim());
        donMuaRepository.save(dm);
        ghiNhatKy(nv, "huy", dm);
        return dungChiTiet(dm);
    }

    // ======================= NHẬN HÀNG =======================

    @Override
    @Transactional
    public DonMuaChiTietResponse nhanHang(Long id, NhanHangDonMuaRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        DonMua dm = khoaDonMua(id);
        if (!DA_XAC_NHAN.equals(dm.getTrangThai()) && !NHAN_MOT_PHAN.equals(dm.getTrangThai())) {
            throw new BusinessException("Chỉ nhận hàng được khi đơn đã xác nhận và chưa nhận đủ");
        }
        Kho kho = dm.getKho();
        kiemTraQuyenKho(nv, kho);

        List<ChiTietDonMua> dongDon = chiTietDonMuaRepository.findByDonMua_Id(id);
        Map<Long, ChiTietDonMua> theoId = dongDon.stream()
                .collect(Collectors.toMap(ChiTietDonMua::getId, Function.identity()));

        // Kiểm tra toàn bộ trước khi ghi bất cứ thứ gì
        Set<Long> daThay = new HashSet<>();
        List<NhanHangDonMuaRequest.Dong> dsNhan = new ArrayList<>(req.getChiTiet());
        for (NhanHangDonMuaRequest.Dong d : dsNhan) {
            ChiTietDonMua l = theoId.get(d.getChiTietId());
            if (l == null) {
                throw new BusinessException("Dòng hàng " + d.getChiTietId() + " không thuộc đơn mua này");
            }
            if (!daThay.add(d.getChiTietId())) {
                throw new BusinessException("Dòng hàng " + l.getMatHang().getTenMatHang()
                        + " bị chọn 2 lần, hãy gộp thành 1 dòng");
            }
            int conLai = l.getSoLuongDat() - l.getSoLuongDaNhan();
            if (d.getSoLuongNhan() > conLai) {
                throw new BusinessException(l.getMatHang().getTenMatHang() + " chỉ còn " + conLai
                        + " chưa nhận, không thể nhận " + d.getSoLuongNhan());
            }
        }
        // Khóa tồn kho theo thứ tự mặt hàng cố định để tránh deadlock
        dsNhan.sort(Comparator.comparing(d -> theoId.get(d.getChiTietId()).getMatHang().getId()));

        String ghiChuPhieu = "Nhận hàng theo đơn mua " + dm.getMaDonMua()
                + (req.getGhiChu() != null && !req.getGhiChu().isBlank() ? ". " + req.getGhiChu().trim() : "");
        PhieuKho pk = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho")
                .kho(kho).donMua(dm)
                .nhanVienGiaoNhan(nv)
                .bienSoXeDoiTac(lamSach(req.getBienSoXe()))
                .ngayGio(LocalDateTime.now())
                .trangThai("da_hoan_thanh")
                .ghiChu(ghiChuPhieu)
                .build());

        for (NhanHangDonMuaRequest.Dong d : dsNhan) {
            ChiTietDonMua dong = theoId.get(d.getChiTietId());
            MatHang mh = dong.getMatHang();

            LoHang lo = loHangRepository.save(LoHang.builder()
                    .maLo(phieuKhoRepository.getMaChungTu("LO", "LO-"))
                    .matHang(mh).kho(kho).ncc(dm.getNcc())
                    .chiTietDonMua(dong)
                    .soLuongNhap(d.getSoLuongNhan())
                    .soLuongConLai(d.getSoLuongNhan())
                    .ngayNhap(LocalDate.now())
                    .trangThai("con_hang")
                    .mucDichNhap("cho_tai_che".equals(mh.getLoaiMatHang()) ? "cho_tai_che" : "ban_thanh_pham")
                    .donGiaNhap(dong.getDonGia())
                    .build());

            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(pk).matHang(mh).lo(lo)
                    .soLuong(d.getSoLuongNhan())
                    .ghiChu(d.getGhiChu())
                    .build());

            tonKhoNghiepVu.cong(mh, kho, d.getSoLuongNhan());
            dong.setSoLuongDaNhan(dong.getSoLuongDaNhan() + d.getSoLuongNhan());
            chiTietDonMuaRepository.save(dong);
        }

        boolean nhanDu = dongDon.stream().allMatch(l -> l.getSoLuongDaNhan() >= l.getSoLuongDat());
        dm.setTrangThai(nhanDu ? NHAN_DU : NHAN_MOT_PHAN);
        donMuaRepository.save(dm);

        ghiNhatKy(nv, "nhan_hang", dm);
        thongBaoTrigger.phieuNhapHoanThanh(pk.getId(), pk.getMaPhieu(), kho.getTenKho(), dsNhan.size(), nv);
        return dungChiTiet(dm);
    }

    // ======================= HELPER =======================

    private List<ChiTietDonMua> taoDong(List<TaoDonMuaRequest.Dong> ds) {
        Set<Long> daCo = new HashSet<>();
        List<ChiTietDonMua> ketQua = new ArrayList<>();
        for (TaoDonMuaRequest.Dong d : ds) {
            MatHang mh = matHangRepository.findById(d.getMatHangId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Mặt hàng", d.getMatHangId()));
            if (!"dang_kinh_doanh".equals(mh.getTrangThai())) {
                throw new BusinessException("Mặt hàng " + mh.getTenMatHang() + " đã ngừng kinh doanh");
            }
            if ("phe_lieu".equals(mh.getLoaiMatHang())) {
                throw new BusinessException("Phế liệu không mua qua đơn mua (chỉ thu về từ tháo dỡ)");
            }
            // Quy tắc nghiệp vụ: pallet cũ đưa vào tái chế chỉ nhận pallet GỖ
            if ("cho_tai_che".equals(mh.getLoaiMatHang()) && !"go".equals(mh.getChatLieu())) {
                throw new BusinessException("Pallet cũ nhập về để tái chế chỉ nhận pallet gỗ: " + mh.getTenMatHang());
            }
            if (!daCo.add(mh.getId())) {
                throw new BusinessException("Mặt hàng " + mh.getTenMatHang()
                        + " xuất hiện 2 lần trong đơn, hãy gộp thành 1 dòng");
            }
            ketQua.add(ChiTietDonMua.builder()
                    .matHang(mh)
                    .soLuongDat(d.getSoLuong())
                    .soLuongDaNhan(0)
                    .donGia(d.getDonGia())
                    .build());
        }
        return ketQua;
    }

    private BigDecimal tinhTong(List<ChiTietDonMua> dong) {
        return dong.stream()
                .map(l -> l.getDonGia().multiply(BigDecimal.valueOf(l.getSoLuongDat())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void kiemTraNgayDuKien(LocalDate ngayDuKien, LocalDate ngayDat) {
        if (ngayDuKien != null && ngayDuKien.isBefore(ngayDat)) {
            throw new BusinessException("Ngày dự kiến nhận không được trước ngày đặt hàng");
        }
    }

    private void kiemTraQuyenKho(NhanVien nv, Kho kho) {
        phanQuyenKho.kiemTra(nv, kho);
    }

    private boolean laQuanLy(NhanVien nv) {
        return phanQuyenKho.laQuanLy(nv);
    }

    private void ghiNhatKy(NhanVien nv, String hanhDong, DonMua dm) {
        nhatKyService.ghi(nv, "don_mua", hanhDong, "don_mua", dm.getId());
    }

    private Kho xacDinhKho(Long khoId, NhanVien nv) {
        if (khoId != null) {
            return khoRepository.findById(khoId).orElseThrow(() -> ResourceNotFoundException.of("Kho", khoId));
        }
        if (nv.getKho() != null) return nv.getKho();
        throw new BusinessException("Tài khoản chưa được gán kho, vui lòng chọn kho nhận hàng");
    }

    private NhaCungCap timNccHopTac(Long id) {
        NhaCungCap ncc = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhà cung cấp", id));
        if (!"dang_hop_tac".equals(ncc.getTrangThai())) {
            throw new BusinessException("Nhà cung cấp " + ncc.getTenNcc() + " đã ngừng hợp tác");
        }
        return ncc;
    }

    private DonMua timDonMua(Long id) {
        return donMuaRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Đơn mua", id));
    }

    private DonMua khoaDonMua(Long id) {
        return donMuaRepository.findForUpdate(id).orElseThrow(() -> ResourceNotFoundException.of("Đơn mua", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }


    private boolean laTreHan(DonMua d) {
        return d.getNgayDuKienNhan() != null
                && d.getNgayDuKienNhan().isBefore(LocalDate.now())
                && (DA_XAC_NHAN.equals(d.getTrangThai()) || NHAN_MOT_PHAN.equals(d.getTrangThai()));
    }

    private DonMuaResponse toDong(DonMua d, List<ChiTietDonMua> dong) {
        return DonMuaResponse.builder()
                .id(d.getId()).maDonMua(d.getMaDonMua())
                .tenNhaCungCap(d.getNcc().getTenNcc())
                .tenKho(d.getKho().getTenKho())
                .tenNhanVienLap(d.getNhanVienLap().getHoTen())
                .ngayDat(d.getNgayDat()).ngayDuKienNhan(d.getNgayDuKienNhan())
                .trangThai(d.getTrangThai()).treHan(laTreHan(d))
                .tongTien(d.getTongTien())
                .tongSoLuongDat(dong.stream().mapToInt(ChiTietDonMua::getSoLuongDat).sum())
                .tongSoLuongDaNhan(dong.stream().mapToInt(ChiTietDonMua::getSoLuongDaNhan).sum())
                .build();
    }

    private DonMuaChiTietResponse dungChiTiet(DonMua d) {
        List<ChiTietDonMua> lines = chiTietDonMuaRepository.findByDonMua_Id(d.getId()).stream()
                .sorted(Comparator.comparing(ChiTietDonMua::getId)).toList();

        List<DonMuaChiTietResponse.Dong> dong = new ArrayList<>();
        int stt = 1;
        for (ChiTietDonMua l : lines) {
            MatHang mh = l.getMatHang();
            dong.add(DonMuaChiTietResponse.Dong.builder()
                    .id(l.getId()).stt(stt++)
                    .matHangId(mh.getId()).maMatHang(mh.getMaMatHang()).tenMatHang(mh.getTenMatHang())
                    .loaiMatHang(mh.getLoaiMatHang()).donViTinh(mh.getDonViTinh())
                    .soLuongDat(l.getSoLuongDat()).soLuongDaNhan(l.getSoLuongDaNhan())
                    .soLuongConLai(l.getSoLuongDat() - l.getSoLuongDaNhan())
                    .donGia(l.getDonGia())
                    .thanhTien(l.getDonGia().multiply(BigDecimal.valueOf(l.getSoLuongDat())))
                    .build());
        }

        List<DonMuaChiTietResponse.PhieuNhap> phieuNhap = phieuKhoRepository.findByDonMua_Id(d.getId()).stream()
                .sorted(Comparator.comparing(PhieuKho::getNgayGio).reversed())
                .map(pk -> DonMuaChiTietResponse.PhieuNhap.builder()
                        .id(pk.getId()).maPhieu(pk.getMaPhieu()).ngayGio(pk.getNgayGio())
                        .trangThai(pk.getTrangThai())
                        .tongSoLuong(chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId()).stream()
                                .mapToInt(ChiTietPhieuKho::getSoLuong).sum())
                        .build())
                .toList();

        NhaCungCap n = d.getNcc();
        return DonMuaChiTietResponse.builder()
                .id(d.getId()).maDonMua(d.getMaDonMua()).trangThai(d.getTrangThai())
                .treHan(laTreHan(d))
                .ngayDat(d.getNgayDat()).ngayDuKienNhan(d.getNgayDuKienNhan())
                .ghiChu(d.getGhiChu()).lyDoHuy(d.getLyDoHuy())
                .tongTien(d.getTongTien()).createdAt(d.getCreatedAt())
                .khoId(d.getKho().getId()).tenKho(d.getKho().getTenKho())
                .nhaCungCap(DonMuaChiTietResponse.DoiTac.builder()
                        .id(n.getId()).ma(n.getMaNcc()).ten(n.getTenNcc()).mst(n.getMst())
                        .sdt(n.getSdt()).diaChi(n.getDiaChi()).nguoiLienHe(n.getNguoiLienHe()).build())
                .tenNhanVienLap(d.getNhanVienLap().getHoTen())
                .tenNguoiXacNhan(d.getNguoiXacNhan() != null ? d.getNguoiXacNhan().getHoTen() : null)
                .chiTiet(dong).phieuNhap(phieuNhap)
                .build();
    }
}