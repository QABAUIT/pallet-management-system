package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.TaoPhieuXuLyThemRequest;
import com.pallet.backend.dto.response.PhieuXuLyThemResponse;
import com.pallet.backend.dto.response.TonCanXuLyResponse;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.PhieuXuLyThem;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.PhieuXuLyThemRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import com.pallet.backend.service.PhieuXuLyThemService;
import com.pallet.backend.service.QuyTacTaiChe;
import com.pallet.backend.service.TonKhoNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuXuLyThemServiceImpl implements PhieuXuLyThemService {

    private static final String NHAP = "nhap";
    private static final String DA_DUYET = "da_duyet";
    private static final String DA_HUY = "da_huy";

    private final PhieuXuLyThemRepository phieuXuLyThemRepository;
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
    public List<PhieuXuLyThemResponse> layDanhSach(String trangThai, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<PhieuXuLyThem> ds = (trangThai == null || trangThai.isBlank())
                ? phieuXuLyThemRepository.timTatCa()
                : phieuXuLyThemRepository.timTheoTrangThai(trangThai);
        return ds.stream()
                .filter(x -> phanQuyenKho.duocThaoTac(nv, x.getKho()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuXuLyThemResponse layChiTiet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuXuLyThem x = timPhieu(id);
        phanQuyenKho.kiemTra(nv, x.getKho());
        return toResponse(x);
    }

    @Override
    @Transactional(readOnly = true)
    public TonCanXuLyResponse layTonCanXuLy(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<TonCanXuLyResponse.Dong> dong = new ArrayList<>();
        for (Kho kho : khoRepository.findAll()) {
            if (!phanQuyenKho.duocThaoTac(nv, kho)) continue;
            for (String loai : new String[]{"thanh", "cuc"}) {
                MatHang mh = layMatHang(QuyTacTaiChe.maNguon(loai));
                int ton = tonKhoRepository.findByMatHang_IdAndKho_Id(mh.getId(), kho.getId())
                        .map(t -> t.getSoLuongTonKho()).orElse(0);
                if (ton > 0) {
                    dong.add(TonCanXuLyResponse.Dong.builder()
                            .khoId(kho.getId()).tenKho(kho.getTenKho())
                            .loaiNguon(loai).tenMatHang(mh.getTenMatHang()).soLuongTon(ton).build());
                }
            }
        }
        return TonCanXuLyResponse.builder().dong(dong).build();
    }

    // ======================= TẠO / SỬA / HỦY =======================

    @Override
    @Transactional
    public PhieuXuLyThemResponse taoMoi(TaoPhieuXuLyThemRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = timKho(req.getKhoId());
        phanQuyenKho.kiemTra(nv, kho);

        int datLai = z(req.getSoDatLai());
        int pheLieu = z(req.getSoPheLieu());
        int kg = z(req.getKgPheLieu());
        QuyTacTaiChe.kiemTraXuLyThem(req.getLoaiNguon(), datLai, pheLieu, kg, req.getLyDo());

        PhieuXuLyThem x = phieuXuLyThemRepository.save(PhieuXuLyThem.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PTC", "XL-"))
                .kho(kho).nhanVienThucHien(nv).loaiNguon(req.getLoaiNguon())
                .soDatLai(datLai).soPheLieu(pheLieu).kgPheLieu(kg)
                .lyDo(req.getLyDo().trim())
                .ngayThucHien(LocalDate.now()).trangThai(NHAP)
                .createdAt(LocalDateTime.now())
                .build());

        nhatKyService.ghi(nv, "xu_ly_them", "tao_moi", "phieu_xu_ly_them", x.getId());
        return toResponse(x);
    }

    @Override
    @Transactional
    public PhieuXuLyThemResponse capNhat(Long id, TaoPhieuXuLyThemRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuXuLyThem x = khoaPhieu(id);
        if (!NHAP.equals(x.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được phiếu xử lý đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, x);
        if (!x.getKho().getId().equals(req.getKhoId())) {
            throw new BusinessException("Không đổi được kho của phiếu, hãy hủy và tạo phiếu mới");
        }

        int datLai = z(req.getSoDatLai());
        int pheLieu = z(req.getSoPheLieu());
        int kg = z(req.getKgPheLieu());
        QuyTacTaiChe.kiemTraXuLyThem(req.getLoaiNguon(), datLai, pheLieu, kg, req.getLyDo());

        x.setLoaiNguon(req.getLoaiNguon());
        x.setSoDatLai(datLai);
        x.setSoPheLieu(pheLieu);
        x.setKgPheLieu(kg);
        x.setLyDo(req.getLyDo().trim());
        x = phieuXuLyThemRepository.save(x);

        nhatKyService.ghi(nv, "xu_ly_them", "cap_nhat", "phieu_xu_ly_them", x.getId());
        return toResponse(x);
    }

    @Override
    @Transactional
    public PhieuXuLyThemResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy phiếu xử lý");
        }
        PhieuXuLyThem x = khoaPhieu(id);
        if (!NHAP.equals(x.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được phiếu xử lý đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, x);

        String ly = lyDo.trim();
        x.setTrangThai(DA_HUY);
        x.setLyDoHuy(ly.length() > 255 ? ly.substring(0, 255) : ly);
        x = phieuXuLyThemRepository.save(x);

        nhatKyService.ghi(nv, "xu_ly_them", "huy", "phieu_xu_ly_them", x.getId());
        return toResponse(x);
    }

    // ======================= DUYỆT =======================

    @Override
    @Transactional
    public PhieuXuLyThemResponse duyet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuXuLyThem x = khoaPhieu(id);
        if (!NHAP.equals(x.getTrangThai())) {
            throw new BusinessException("Phiếu xử lý không ở trạng thái nháp nên không duyệt được");
        }

        int datLai = x.getSoDatLai();
        int pheLieu = x.getSoPheLieu();
        int kg = x.getKgPheLieu();
        QuyTacTaiChe.kiemTraXuLyThem(x.getLoaiNguon(), datLai, pheLieu, kg, x.getLyDo());

        Kho kho = x.getKho();
        MatHang nguon = layMatHang(QuyTacTaiChe.maNguon(x.getLoaiNguon()));
        MatHang dich = layMatHang(QuyTacTaiChe.maDich(x.getLoaiNguon()));
        MatHang pheLieuMh = layMatHang(QuyTacTaiChe.MA_PHE_LIEU);
        int tongXuat = datLai + pheLieu;

        // Trừ hàng nguồn trước: không đủ tồn thì dừng, không đổi gì (TC06/TC09)
        tonKhoNghiepVu.tru(nguon, kho, tongXuat);

        PhieuKho xuat = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PX", "PX-"))
                .loaiPhieu("xuat_kho").kho(kho)
                .nhanVienGiaoNhan(x.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Xuất hàng cần xử lý thêm theo phiếu " + x.getMaPhieu() + ": " + x.getLyDo())
                .build());
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(xuat).matHang(nguon).soLuong(tongXuat).ghiChu(x.getMaPhieu()).build());

        PhieuKho nhap = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho").kho(kho)
                .nhanVienGiaoNhan(x.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Nhập kết quả xử lý thêm theo phiếu " + x.getMaPhieu())
                .build());
        if (datLai > 0) {
            tonKhoNghiepVu.cong(dich, kho, datLai);
            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(nhap).matHang(dich).soLuong(datLai).ghiChu("Xử lý xong, dùng lại được").build());
        }
        if (kg > 0) {
            tonKhoNghiepVu.cong(pheLieuMh, kho, kg);
            chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                    .phieuKho(nhap).matHang(pheLieuMh).soLuong(kg)
                    .ghiChu("Phế liệu từ " + pheLieu + " chiếc bị bỏ (kg)").build());
        }

        x.setTrangThai(DA_DUYET);
        x.setNguoiDuyet(nv);
        x.setThoiGianDuyet(LocalDateTime.now());
        x = phieuXuLyThemRepository.save(x);

        nhatKyService.ghi(nv, "xu_ly_them", "duyet", "phieu_xu_ly_them", x.getId());
        return toResponse(x);
    }

    // ======================= HELPER =======================

    private void kiemTraNguoiSua(NhanVien nv, PhieuXuLyThem x) {
        boolean laNguoiTao = x.getNhanVienThucHien().getId().equals(nv.getId());
        if (!phanQuyenKho.laQuanLy(nv) && !laNguoiTao) {
            throw new BusinessException("Chỉ người lập phiếu hoặc quản lý mới được sửa/hủy phiếu này");
        }
        phanQuyenKho.kiemTra(nv, x.getKho());
    }

    private int z(Integer v) {
        return v == null ? 0 : v;
    }

    private Kho timKho(Long id) {
        return khoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Kho", id));
    }

    private MatHang layMatHang(String ma) {
        return matHangRepository.findByMaMatHang(ma)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng hệ thống mã " + ma
                        + ", hãy kiểm tra lại dữ liệu mặt hàng"));
    }

    private PhieuXuLyThem timPhieu(Long id) {
        return phieuXuLyThemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu xử lý", id));
    }

    private PhieuXuLyThem khoaPhieu(Long id) {
        return phieuXuLyThemRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu xử lý", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private PhieuXuLyThemResponse toResponse(PhieuXuLyThem x) {
        return PhieuXuLyThemResponse.builder()
                .id(x.getId()).maPhieu(x.getMaPhieu()).trangThai(x.getTrangThai())
                .khoId(x.getKho().getId()).tenKho(x.getKho().getTenKho())
                .loaiNguon(x.getLoaiNguon())
                .tenMatHangNguon(layMatHang(QuyTacTaiChe.maNguon(x.getLoaiNguon())).getTenMatHang())
                .tenMatHangDich(layMatHang(QuyTacTaiChe.maDich(x.getLoaiNguon())).getTenMatHang())
                .soDatLai(x.getSoDatLai()).soPheLieu(x.getSoPheLieu()).kgPheLieu(x.getKgPheLieu())
                .lyDo(x.getLyDo())
                .tenNguoiThucHien(x.getNhanVienThucHien().getHoTen())
                .tenNguoiDuyet(x.getNguoiDuyet() != null ? x.getNguoiDuyet().getHoTen() : null)
                .ngayThucHien(x.getNgayThucHien()).thoiGianDuyet(x.getThoiGianDuyet())
                .createdAt(x.getCreatedAt()).lyDoHuy(x.getLyDoHuy())
                .build();
    }
}