package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.TaoPhieuDongPalletRequest;
import com.pallet.backend.dto.response.DanhMucDongPalletResponse;
import com.pallet.backend.dto.response.DinhMucResponse;
import com.pallet.backend.dto.response.DuKienDongPalletResponse;
import com.pallet.backend.dto.response.PhieuDongPalletResponse;
import com.pallet.backend.entity.ChiTietDinhMuc;
import com.pallet.backend.entity.ChiTietDongPallet;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.DinhMuc;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuDongPallet;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietDinhMucRepository;
import com.pallet.backend.repository.ChiTietDongPalletRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.DinhMucRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuDongPalletRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.service.DinhMucService;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import com.pallet.backend.service.PhieuDongPalletService;
import com.pallet.backend.service.QuyTacDongPallet;
import com.pallet.backend.service.QuyTacTaiChe;
import com.pallet.backend.service.TonKhoNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhieuDongPalletServiceImpl implements PhieuDongPalletService {

    private static final String NHAP = "nhap";
    private static final String DA_DUYET = "da_duyet";
    private static final String DA_HUY = "da_huy";

    private final PhieuDongPalletRepository phieuDongPalletRepository;
    private final ChiTietDongPalletRepository chiTietDongPalletRepository;
    private final DinhMucRepository dinhMucRepository;
    private final ChiTietDinhMucRepository chiTietDinhMucRepository;
    private final DinhMucService dinhMucService;
    private final KhoRepository khoRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final TonKhoNghiepVu tonKhoNghiepVu;
    private final PhanQuyenKho phanQuyenKho;
    private final NhatKyService nhatKyService;

    // ======================= ĐỌC =======================

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDongPalletResponse> layDanhSach(String trangThai, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<PhieuDongPallet> ds = (trangThai == null || trangThai.isBlank())
                ? phieuDongPalletRepository.timTatCa()
                : phieuDongPalletRepository.timTheoTrangThai(trangThai);
        return ds.stream()
                .filter(p -> phanQuyenKho.duocThaoTac(nv, p.getKho()))
                .map(p -> toResponse(p, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuDongPalletResponse layChiTiet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDongPallet p = phieuDongPalletRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu đóng pallet", id));
        phanQuyenKho.kiemTra(nv, p.getKho());
        return toResponse(p, true);
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucDongPalletResponse layDanhMuc() {
        List<DinhMucResponse> dm = dinhMucService.layDanhSach(true);
        List<DanhMucDongPalletResponse.VatTu> vatTu = matHangRepository.findByTrangThai("hoat_dong").stream()
                .filter(m -> !"kg".equals(m.getDonViTinh()))
                .filter(m -> !QuyTacTaiChe.MA_CHO_PHAN_LOAI.equals(m.getMaMatHang())
                        && !QuyTacTaiChe.MA_CHO_THAO_DO.equals(m.getMaMatHang()))
                .map(m -> DanhMucDongPalletResponse.VatTu.builder()
                        .id(m.getId()).maMatHang(m.getMaMatHang()).tenMatHang(m.getTenMatHang()).build())
                .toList();
        return DanhMucDongPalletResponse.builder().dinhMuc(dm).vatTu(vatTu).build();
    }

    @Override
    @Transactional(readOnly = true)
    public DuKienDongPalletResponse duKien(Long dinhMucId, Long khoId, int soLuong, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = timKho(khoId);
        phanQuyenKho.kiemTra(nv, kho);
        DinhMuc dm = dinhMucRepository.findById(dinhMucId)
                .orElseThrow(() -> ResourceNotFoundException.of("Định mức", dinhMucId));

        List<DuKienDongPalletResponse.Dong> dong = new ArrayList<>();
        boolean du = true;
        for (ChiTietDinhMuc c : chiTietDinhMucRepository.timTheoDinhMuc(dm.getId())) {
            int nhuCau = QuyTacDongPallet.tinhNhuCau(c.getSoLuong(), soLuong);
            int khaDung = khaDung(c.getMatHang(), kho);
            int thieu = Math.max(0, nhuCau - khaDung);
            if (thieu > 0) du = false;
            dong.add(DuKienDongPalletResponse.Dong.builder()
                    .matHangId(c.getMatHang().getId()).maMatHang(c.getMatHang().getMaMatHang())
                    .tenMatHang(c.getMatHang().getTenMatHang())
                    .nhuCau(nhuCau).khaDung(khaDung).thieu(thieu).build());
        }
        return DuKienDongPalletResponse.builder()
                .dinhMucId(dm.getId()).khoId(kho.getId()).soLuongSanXuat(soLuong)
                .duVatTu(du).chiTiet(dong).build();
    }

    // ======================= TẠO / SỬA / HỦY =======================

    @Override
    @Transactional
    public PhieuDongPalletResponse taoMoi(TaoPhieuDongPalletRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = timKho(req.getKhoId());
        phanQuyenKho.kiemTra(nv, kho);
        DinhMuc dm = layDinhMucHoatDong(req.getDinhMucId());
        List<ChiTietDongPallet> dong = dungDong(dm, req);

        PhieuDongPallet p = phieuDongPalletRepository.save(PhieuDongPallet.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PTC", "DP-"))
                .dinhMuc(dm).matHangThanhPham(dm.getMatHangThanhPham()).kho(kho)
                .nhanVienThucHien(nv).soLuongSanXuat(req.getSoLuongSanXuat())
                .ngayThucHien(LocalDate.now()).trangThai(NHAP)
                .ghiChu(lamSach(req.getGhiChu())).createdAt(LocalDateTime.now())
                .build());
        luuDong(p, dong);

        nhatKyService.ghi(nv, "dong_pallet", "tao_moi", "phieu_dong_pallet", p.getId());
        return toResponse(p, true);
    }

    @Override
    @Transactional
    public PhieuDongPalletResponse capNhat(Long id, TaoPhieuDongPalletRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDongPallet p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được phiếu đóng pallet đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, p);
        if (!p.getKho().getId().equals(req.getKhoId())) {
            throw new BusinessException("Không đổi được kho của phiếu, hãy hủy và tạo phiếu mới");
        }
        DinhMuc dm = layDinhMucHoatDong(req.getDinhMucId());
        List<ChiTietDongPallet> dong = dungDong(dm, req);

        p.setDinhMuc(dm);
        p.setMatHangThanhPham(dm.getMatHangThanhPham());
        p.setSoLuongSanXuat(req.getSoLuongSanXuat());
        p.setGhiChu(lamSach(req.getGhiChu()));
        p = phieuDongPalletRepository.save(p);

        chiTietDongPalletRepository.xoaTheoPhieu(p.getId());
        luuDong(p, dong);

        nhatKyService.ghi(nv, "dong_pallet", "cap_nhat", "phieu_dong_pallet", p.getId());
        return toResponse(p, true);
    }

    @Override
    @Transactional
    public PhieuDongPalletResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy phiếu đóng pallet");
        }
        PhieuDongPallet p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được phiếu đóng pallet đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, p);

        String ly = lyDo.trim();
        p.setTrangThai(DA_HUY);
        p.setLyDoHuy(ly.length() > 255 ? ly.substring(0, 255) : ly);
        p = phieuDongPalletRepository.save(p);

        nhatKyService.ghi(nv, "dong_pallet", "huy", "phieu_dong_pallet", p.getId());
        return toResponse(p, true);
    }

    // ======================= DUYỆT =======================

    @Override
    @Transactional
    public PhieuDongPalletResponse duyet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDongPallet p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Phiếu đóng pallet không ở trạng thái nháp nên không duyệt được");
        }

        List<ChiTietDongPallet> dong = chiTietDongPalletRepository.timTheoPhieu(p.getId());
        int tong = dong.stream().mapToInt(ChiTietDongPallet::getSoLuongThucTe).sum();
        QuyTacDongPallet.kiemTraCoVatTu(tong);

        Kho kho = p.getKho();
        MatHang thanhPham = p.getMatHangThanhPham();

        PhieuKho xuat = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PX", "PX-"))
                .loaiPhieu("xuat_kho").kho(kho)
                .nhanVienGiaoNhan(p.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Xuất vật tư đóng pallet theo phiếu " + p.getMaPhieu())
                .build());

        // Trừ kho từng vật tư theo số THỰC TẾ; thiếu tồn thì cả giao dịch bị hủy (TC06/TC09)
        for (ChiTietDongPallet c : dong) {
            if (c.getSoLuongThucTe() <= 0) continue;
            tonKhoNghiepVu.tru(c.getMatHang(), kho, c.getSoLuongThucTe());
            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(xuat).matHang(c.getMatHang()).soLuong(c.getSoLuongThucTe())
                    .ghiChu(c.getChenhLech() != null && c.getChenhLech() != 0
                            ? "Lệch định mức " + c.getChenhLech() + ": " + c.getLyDoChenhLech()
                            : "Đúng định mức").build());
        }

        PhieuKho nhap = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho").kho(kho)
                .nhanVienGiaoNhan(p.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Nhập pallet tái chế thành phẩm theo phiếu " + p.getMaPhieu())
                .build());
        tonKhoNghiepVu.cong(thanhPham, kho, p.getSoLuongSanXuat());
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(nhap).matHang(thanhPham).soLuong(p.getSoLuongSanXuat())
                .ghiChu(p.getMaPhieu()).build());

        p.setTrangThai(DA_DUYET);
        p.setNguoiDuyet(nv);
        p.setThoiGianDuyet(LocalDateTime.now());
        p = phieuDongPalletRepository.save(p);

        nhatKyService.ghi(nv, "dong_pallet", "duyet", "phieu_dong_pallet", p.getId());
        return toResponse(p, true);
    }

    // ======================= HELPER =======================

    /** Dựng các dòng vật tư: mặc định đúng định mức, cho ghi đè và thêm vật tư ngoài định mức. */
    private List<ChiTietDongPallet> dungDong(DinhMuc dm, TaoPhieuDongPalletRequest req) {
        int soSx = req.getSoLuongSanXuat();
        Map<Long, ChiTietDongPallet> theoVatTu = new LinkedHashMap<>();

        for (ChiTietDinhMuc c : chiTietDinhMucRepository.timTheoDinhMuc(dm.getId())) {
            int nhuCau = QuyTacDongPallet.tinhNhuCau(c.getSoLuong(), soSx);
            theoVatTu.put(c.getMatHang().getId(), ChiTietDongPallet.builder()
                    .matHang(c.getMatHang()).soLuongDinhMuc(nhuCau).soLuongThucTe(nhuCau).build());
        }

        if (req.getChiTiet() != null) {
            java.util.Set<Long> daThay = new java.util.HashSet<>();
            for (TaoPhieuDongPalletRequest.Dong d : req.getChiTiet()) {
                if (!daThay.add(d.getMatHangId())) {
                    throw new BusinessException("Vật tư bị trùng dòng trong phiếu");
                }
                ChiTietDongPallet dong = theoVatTu.get(d.getMatHangId());
                if (dong == null) { // vật tư ngoài định mức
                    MatHang vt = matHangRepository.findById(d.getMatHangId())
                            .orElseThrow(() -> ResourceNotFoundException.of("Vật tư", d.getMatHangId()));
                    DinhMucServiceImpl.kiemTraVatTu(vt, dm.getMatHangThanhPham().getId());
                    dong = ChiTietDongPallet.builder().matHang(vt).soLuongDinhMuc(0).soLuongThucTe(0).build();
                    theoVatTu.put(vt.getId(), dong);
                }
                dong.setSoLuongThucTe(d.getSoLuongThucTe());
                dong.setLyDoChenhLech(lamSach(d.getLyDoChenhLech()));
            }
        }

        int tong = 0;
        for (ChiTietDongPallet c : theoVatTu.values()) {
            QuyTacDongPallet.kiemTraDong(c.getMatHang().getTenMatHang(),
                    c.getSoLuongDinhMuc(), c.getSoLuongThucTe(), c.getLyDoChenhLech());
            if (c.getSoLuongThucTe() == c.getSoLuongDinhMuc()) c.setLyDoChenhLech(null);
            tong += c.getSoLuongThucTe();
        }
        QuyTacDongPallet.kiemTraCoVatTu(tong);
        return new ArrayList<>(theoVatTu.values());
    }

    private void luuDong(PhieuDongPallet p, List<ChiTietDongPallet> dong) {
        dong.forEach(c -> c.setPhieuDongPallet(p));
        chiTietDongPalletRepository.saveAll(dong);
    }

    private int khaDung(MatHang mh, Kho kho) {
        return tonKhoRepository.findByMatHang_IdAndKho_Id(mh.getId(), kho.getId())
                .map(t -> t.getSoLuongTonKho() - t.getSoLuongDaGiuCho()).orElse(0);
    }

    private DinhMuc layDinhMucHoatDong(Long id) {
        DinhMuc dm = dinhMucRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Định mức", id));
        if (!"hoat_dong".equals(dm.getTrangThai())) {
            throw new BusinessException("Định mức " + dm.getMaDinhMuc() + " đã ngừng sử dụng");
        }
        return dm;
    }

    private void kiemTraNguoiSua(NhanVien nv, PhieuDongPallet p) {
        boolean laNguoiTao = p.getNhanVienThucHien().getId().equals(nv.getId());
        if (!phanQuyenKho.laQuanLy(nv) && !laNguoiTao) {
            throw new BusinessException("Chỉ người lập phiếu hoặc quản lý mới được sửa/hủy phiếu này");
        }
        phanQuyenKho.kiemTra(nv, p.getKho());
    }

    private Kho timKho(Long id) {
        return khoRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Kho", id));
    }

    private PhieuDongPallet khoaPhieu(Long id) {
        return phieuDongPalletRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu đóng pallet", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private PhieuDongPalletResponse toResponse(PhieuDongPallet p, boolean chiTiet) {
        List<ChiTietDongPallet> dong = chiTietDongPalletRepository.timTheoPhieu(p.getId());
        int soLech = (int) dong.stream().filter(c -> c.getSoLuongThucTe() != c.getSoLuongDinhMuc()).count();

        PhieuDongPalletResponse.PhieuDongPalletResponseBuilder b = PhieuDongPalletResponse.builder()
                .id(p.getId()).maPhieu(p.getMaPhieu()).trangThai(p.getTrangThai())
                .dinhMucId(p.getDinhMuc().getId()).tenDinhMuc(p.getDinhMuc().getTenDinhMuc())
                .thanhPhamId(p.getMatHangThanhPham().getId()).tenThanhPham(p.getMatHangThanhPham().getTenMatHang())
                .khoId(p.getKho().getId()).tenKho(p.getKho().getTenKho())
                .soLuongSanXuat(p.getSoLuongSanXuat())
                .tenNguoiThucHien(p.getNhanVienThucHien().getHoTen())
                .tenNguoiDuyet(p.getNguoiDuyet() != null ? p.getNguoiDuyet().getHoTen() : null)
                .ngayThucHien(p.getNgayThucHien()).thoiGianDuyet(p.getThoiGianDuyet())
                .createdAt(p.getCreatedAt()).ghiChu(p.getGhiChu()).lyDoHuy(p.getLyDoHuy())
                .soDongChenhLech(soLech);

        if (chiTiet) {
            b.chiTiet(dong.stream().sorted(Comparator.comparing(ChiTietDongPallet::getId))
                    .map(c -> PhieuDongPalletResponse.Dong.builder()
                            .matHangId(c.getMatHang().getId()).maMatHang(c.getMatHang().getMaMatHang())
                            .tenMatHang(c.getMatHang().getTenMatHang())
                            .soLuongDinhMuc(c.getSoLuongDinhMuc()).soLuongThucTe(c.getSoLuongThucTe())
                            .chenhLech(QuyTacDongPallet.chenhLech(c.getSoLuongThucTe(), c.getSoLuongDinhMuc()))
                            .lyDoChenhLech(c.getLyDoChenhLech()).build())
                    .toList());
        }
        return b.build();
    }
}