package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.TaoPhieuThaoDoRequest;
import com.pallet.backend.dto.response.DanhMucThaoDoResponse;
import com.pallet.backend.dto.response.PhieuThaoDoResponse;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LoHang;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.PhieuTaiChe;
import com.pallet.backend.entity.PhieuThaoDo;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.PhieuTaiCheRepository;
import com.pallet.backend.repository.PhieuThaoDoRepository;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import com.pallet.backend.service.PhieuThaoDoService;
import com.pallet.backend.service.QuyTacTaiChe;
import com.pallet.backend.service.TonKhoNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuThaoDoServiceImpl implements PhieuThaoDoService {

    private static final String NHAP = "nhap";
    private static final String DA_DUYET = "da_duyet";
    private static final String DA_HUY = "da_huy";

    private final PhieuThaoDoRepository phieuThaoDoRepository;
    private final PhieuTaiCheRepository phieuTaiCheRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final TonKhoNghiepVu tonKhoNghiepVu;
    private final PhanQuyenKho phanQuyenKho;
    private final NhatKyService nhatKyService;

    // ======================= ĐỌC =======================

    @Override
    @Transactional(readOnly = true)
    public List<PhieuThaoDoResponse> layDanhSach(Long phieuTaiCheId, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<PhieuThaoDo> ds = phieuTaiCheId == null
                ? phieuThaoDoRepository.timTatCa()
                : phieuThaoDoRepository.timTheoPhieuTaiChe(phieuTaiCheId);
        return ds.stream()
                .filter(t -> phanQuyenKho.duocThaoTac(nv, t.getKho()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuThaoDoResponse layChiTiet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuThaoDo t = timPhieu(id);
        phanQuyenKho.kiemTra(nv, t.getKho());
        return toResponse(t);
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucThaoDoResponse layDanhMuc(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        var options = phieuTaiCheRepository.timChoThaoDo().stream()
                .filter(p -> phanQuyenKho.duocThaoTac(nv, p.getKho()))
                .map(p -> {
                    int daDuyet = tong(p.getId(), DA_DUYET, -1L);
                    int dangCho = tong(p.getId(), NHAP, -1L);
                    return DanhMucThaoDoResponse.PhieuOption.builder()
                            .id(p.getId()).maPhieu(p.getMaPhieu()).maLo(p.getLoHang().getMaLo())
                            .khoId(p.getKho().getId()).tenKho(p.getKho().getTenKho())
                            .soCanThaoDo(p.getSoThaoDo()).soDaDuyet(daDuyet).soDangCho(dangCho)
                            .soKhaDung(Math.max(0, p.getSoThaoDo() - daDuyet - dangCho))
                            .build();
                })
                .filter(o -> o.getSoKhaDung() > 0)
                .toList();
        return DanhMucThaoDoResponse.builder().phieuTaiChe(options).build();
    }

    // ======================= TẠO / SỬA / HỦY =======================

    @Override
    @Transactional
    public PhieuThaoDoResponse taoMoi(TaoPhieuThaoDoRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuTaiChe p = khoaPhieuTaiChe(req.getPhieuTaiCheId());
        kiemTraPhieuTaiChe(p);
        phanQuyenKho.kiemTra(nv, p.getKho());

        int[] v = layGiaTri(req);
        QuyTacTaiChe.kiemTraThaoDo(req.getSoPalletThaoDo(), v[0], v[1], v[2], v[3], v[4]);
        kiemTraConDuSo(p, req.getSoPalletThaoDo(), -1L);

        PhieuThaoDo t = phieuThaoDoRepository.save(PhieuThaoDo.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PTC", "TD-"))
                .phieuTaiChe(p).kho(p.getKho()).nhanVienThucHien(nv)
                .soPalletThaoDo(req.getSoPalletThaoDo())
                .soThanhNguyen(v[0]).soCucVuong(v[1]).soThanhCanXuLy(v[2]).soCucCanXuLy(v[3])
                .kgPheLieu(v[4])
                .ngayThucHien(LocalDate.now()).trangThai(NHAP)
                .ghiChu(lamSach(req.getGhiChu()))
                .createdAt(LocalDateTime.now())
                .build());

        nhatKyService.ghi(nv, "thao_do", "tao_moi", "phieu_thao_do", t.getId());
        return toResponse(t);
    }

    @Override
    @Transactional
    public PhieuThaoDoResponse capNhat(Long id, TaoPhieuThaoDoRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuThaoDo t = khoaPhieu(id);
        if (!NHAP.equals(t.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được phiếu tháo dỡ đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, t);
        if (!t.getPhieuTaiChe().getId().equals(req.getPhieuTaiCheId())) {
            throw new BusinessException("Không đổi được phiếu tái chế, hãy hủy và tạo phiếu mới");
        }
        PhieuTaiChe p = khoaPhieuTaiChe(t.getPhieuTaiChe().getId());
        kiemTraPhieuTaiChe(p);

        int[] v = layGiaTri(req);
        QuyTacTaiChe.kiemTraThaoDo(req.getSoPalletThaoDo(), v[0], v[1], v[2], v[3], v[4]);
        kiemTraConDuSo(p, req.getSoPalletThaoDo(), t.getId());

        t.setSoPalletThaoDo(req.getSoPalletThaoDo());
        t.setSoThanhNguyen(v[0]);
        t.setSoCucVuong(v[1]);
        t.setSoThanhCanXuLy(v[2]);
        t.setSoCucCanXuLy(v[3]);
        t.setKgPheLieu(v[4]);
        t.setGhiChu(lamSach(req.getGhiChu()));
        t = phieuThaoDoRepository.save(t);

        nhatKyService.ghi(nv, "thao_do", "cap_nhat", "phieu_thao_do", t.getId());
        return toResponse(t);
    }

    @Override
    @Transactional
    public PhieuThaoDoResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy phiếu tháo dỡ");
        }
        PhieuThaoDo t = khoaPhieu(id);
        if (!NHAP.equals(t.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được phiếu tháo dỡ đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, t);

        String ly = lyDo.trim();
        t.setTrangThai(DA_HUY);
        t.setLyDoHuy(ly.length() > 255 ? ly.substring(0, 255) : ly);
        t = phieuThaoDoRepository.save(t);

        nhatKyService.ghi(nv, "thao_do", "huy", "phieu_thao_do", t.getId());
        return toResponse(t);
    }

    // ======================= DUYỆT =======================

    @Override
    @Transactional
    public PhieuThaoDoResponse duyet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuThaoDo t = khoaPhieu(id);
        if (!NHAP.equals(t.getTrangThai())) {
            throw new BusinessException("Phiếu tháo dỡ không ở trạng thái nháp nên không duyệt được");
        }
        PhieuTaiChe p = khoaPhieuTaiChe(t.getPhieuTaiChe().getId());
        kiemTraPhieuTaiChe(p);

        int n = t.getSoPalletThaoDo();
        QuyTacTaiChe.kiemTraThaoDo(n, t.getSoThanhNguyen(), t.getSoCucVuong(),
                t.getSoThanhCanXuLy(), t.getSoCucCanXuLy(), t.getKgPheLieu());

        int daDuyet = tong(p.getId(), DA_DUYET, -1L);
        if (daDuyet + n > p.getSoThaoDo()) {
            throw new BusinessException("Phiếu tái chế " + p.getMaPhieu() + " chỉ còn "
                    + (p.getSoThaoDo() - daDuyet) + " pallet cần tháo dỡ, không thể duyệt đợt " + n + " pallet");
        }

        Kho kho = t.getKho();
        LoHang lo = p.getLoHang();
        MatHang choThaoDo = layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO);

        // Trừ pallet chờ tháo dỡ trước: nếu kho không đủ thì dừng, không đổi gì (TC06/TC09)
        tonKhoNghiepVu.tru(choThaoDo, kho, n);

        PhieuKho xuat = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PX", "PX-"))
                .loaiPhieu("xuat_kho").kho(kho).phieuTaiChe(p)
                .nhanVienGiaoNhan(t.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Xuất pallet để tháo dỡ theo phiếu " + t.getMaPhieu())
                .build());
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(xuat).matHang(choThaoDo).lo(lo).soLuong(n)
                .ghiChu("Lô " + lo.getMaLo()).build());

        PhieuKho nhap = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho").kho(kho).phieuTaiChe(p)
                .nhanVienGiaoNhan(t.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Nhập kết quả tháo dỡ theo phiếu " + t.getMaPhieu())
                .build());

        nhapKetQua(nhap, kho, lo, QuyTacTaiChe.MA_THANH_NGUYEN, t.getSoThanhNguyen(), "Thanh gỗ nguyên");
        nhapKetQua(nhap, kho, lo, QuyTacTaiChe.MA_CUC_VUONG, t.getSoCucVuong(), "Cục gỗ vuông");
        nhapKetQua(nhap, kho, lo, QuyTacTaiChe.MA_THANH_CAN_XU_LY, t.getSoThanhCanXuLy(), "Thanh cần xử lý thêm");
        nhapKetQua(nhap, kho, lo, QuyTacTaiChe.MA_CUC_CAN_XU_LY, t.getSoCucCanXuLy(), "Cục cần xử lý thêm");
        nhapKetQua(nhap, kho, lo, QuyTacTaiChe.MA_PHE_LIEU, t.getKgPheLieu(), "Phế liệu từ tháo dỡ (kg)");

        t.setTrangThai(DA_DUYET);
        t.setNguoiDuyet(nv);
        t.setThoiGianDuyet(LocalDateTime.now());
        t = phieuThaoDoRepository.save(t);

        p.setTrangThai(QuyTacTaiChe.trangThaiSauThaoDo(daDuyet + n, p.getSoThaoDo()));
        phieuTaiCheRepository.save(p);

        nhatKyService.ghi(nv, "thao_do", "duyet", "phieu_thao_do", t.getId());
        return toResponse(t);
    }

    // ======================= HELPER =======================

    private void nhapKetQua(PhieuKho nhap, Kho kho, LoHang lo, String maMatHang, int soLuong, String ghiChu) {
        if (soLuong <= 0) return;
        MatHang mh = layMatHang(maMatHang);
        tonKhoNghiepVu.cong(mh, kho, soLuong);
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(nhap).matHang(mh).lo(lo).soLuong(soLuong).ghiChu(ghiChu).build());
    }

    private void kiemTraPhieuTaiChe(PhieuTaiChe p) {
        if (!"da_phan_loai".equals(p.getTrangThai())) {
            throw new BusinessException("Phiếu tái chế " + p.getMaPhieu()
                    + " không ở trạng thái chờ tháo dỡ (đã phân loại, chưa hoàn tất)");
        }
    }

    /** Số pallet của đợt + các đợt đã duyệt + các đợt nháp khác không được vượt số cần tháo dỡ. */
    private void kiemTraConDuSo(PhieuTaiChe p, int soPallet, Long boQuaId) {
        int daDuyet = tong(p.getId(), DA_DUYET, -1L);
        int dangCho = tong(p.getId(), NHAP, boQuaId);
        int khaDung = p.getSoThaoDo() - daDuyet - dangCho;
        if (soPallet > khaDung) {
            throw new BusinessException("Phiếu tái chế " + p.getMaPhieu() + " có " + p.getSoThaoDo()
                    + " pallet cần tháo dỡ, đã tháo " + daDuyet + ", đang nằm trong đợt nháp khác " + dangCho
                    + ". Chỉ còn " + Math.max(0, khaDung) + " pallet, không thể tạo đợt " + soPallet);
        }
    }

    private int tong(Long phieuId, String trangThai, Long boQuaId) {
        Long v = phieuThaoDoRepository.tongTheoTrangThai(phieuId, trangThai, boQuaId);
        return v == null ? 0 : v.intValue();
    }

    private int[] layGiaTri(TaoPhieuThaoDoRequest r) {
        return new int[]{
                z(r.getSoThanhNguyen()), z(r.getSoCucVuong()),
                z(r.getSoThanhCanXuLy()), z(r.getSoCucCanXuLy()), z(r.getKgPheLieu())};
    }

    private int z(Integer v) {
        return v == null ? 0 : v;
    }

    private void kiemTraNguoiSua(NhanVien nv, PhieuThaoDo t) {
        boolean laNguoiTao = t.getNhanVienThucHien().getId().equals(nv.getId());
        if (!phanQuyenKho.laQuanLy(nv) && !laNguoiTao) {
            throw new BusinessException("Chỉ người lập phiếu hoặc quản lý mới được sửa/hủy phiếu này");
        }
        phanQuyenKho.kiemTra(nv, t.getKho());
    }

    private MatHang layMatHang(String ma) {
        return matHangRepository.findByMaMatHang(ma)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng hệ thống mã " + ma
                        + ", hãy kiểm tra lại dữ liệu mặt hàng"));
    }

    private PhieuThaoDo timPhieu(Long id) {
        return phieuThaoDoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu tháo dỡ", id));
    }

    private PhieuThaoDo khoaPhieu(Long id) {
        return phieuThaoDoRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu tháo dỡ", id));
    }

    private PhieuTaiChe khoaPhieuTaiChe(Long id) {
        return phieuTaiCheRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu tái chế", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private PhieuThaoDoResponse toResponse(PhieuThaoDo t) {
        return PhieuThaoDoResponse.builder()
                .id(t.getId()).maPhieu(t.getMaPhieu()).trangThai(t.getTrangThai())
                .phieuTaiCheId(t.getPhieuTaiChe().getId()).maPhieuTaiChe(t.getPhieuTaiChe().getMaPhieu())
                .maLo(t.getPhieuTaiChe().getLoHang().getMaLo())
                .khoId(t.getKho().getId()).tenKho(t.getKho().getTenKho())
                .tenNguoiThucHien(t.getNhanVienThucHien().getHoTen())
                .tenNguoiDuyet(t.getNguoiDuyet() != null ? t.getNguoiDuyet().getHoTen() : null)
                .soPalletThaoDo(t.getSoPalletThaoDo())
                .soThanhNguyen(t.getSoThanhNguyen()).soCucVuong(t.getSoCucVuong())
                .soThanhCanXuLy(t.getSoThanhCanXuLy()).soCucCanXuLy(t.getSoCucCanXuLy())
                .kgPheLieu(t.getKgPheLieu())
                .ngayThucHien(t.getNgayThucHien()).thoiGianDuyet(t.getThoiGianDuyet())
                .createdAt(t.getCreatedAt()).ghiChu(t.getGhiChu()).lyDoHuy(t.getLyDoHuy())
                .build();
    }
}