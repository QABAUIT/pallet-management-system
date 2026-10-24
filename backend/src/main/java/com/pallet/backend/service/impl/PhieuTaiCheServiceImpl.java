package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.TaoPhieuTaiCheRequest;
import com.pallet.backend.dto.response.DanhMucTaiCheResponse;
import com.pallet.backend.dto.response.PhieuTaiCheResponse;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.ChiTietTaiChe;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LoHang;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.PhieuTaiChe;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.ChiTietTaiCheRepository;
import com.pallet.backend.repository.LoHangRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.PhieuTaiCheRepository;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import com.pallet.backend.service.PhieuTaiCheService;
import com.pallet.backend.service.QuyTacTaiChe;
import com.pallet.backend.service.TonKhoNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuTaiCheServiceImpl implements PhieuTaiCheService {

    private static final String NHAP = "nhap";
    private static final String DA_HUY = "da_huy";

    private final PhieuTaiCheRepository phieuTaiCheRepository;
    private final ChiTietTaiCheRepository chiTietTaiCheRepository;
    private final LoHangRepository loHangRepository;
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
    public List<PhieuTaiCheResponse> layDanhSach(String trangThai, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<PhieuTaiChe> ds = (trangThai == null || trangThai.isBlank())
                ? phieuTaiCheRepository.timTatCa()
                : phieuTaiCheRepository.timTheoTrangThai(trangThai);
        return ds.stream()
                .filter(p -> phanQuyenKho.duocThaoTac(nv, p.getKho()))
                .map(p -> toResponse(p, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuTaiCheResponse layChiTiet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuTaiChe p = timPhieu(id);
        phanQuyenKho.kiemTra(nv, p.getKho());
        return toResponse(p, true);
    }

    @Override
    @Transactional(readOnly = true)
    public DanhMucTaiCheResponse layDanhMuc(String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);

        List<LoHang> dsLo;
        if (phanQuyenKho.laQuanLy(nv)) {
            dsLo = loHangRepository.timLoChoTaiChe();
        } else if (nv.getKho() != null) {
            dsLo = loHangRepository.timLoChoTaiCheTheoKho(nv.getKho().getId());
        } else {
            dsLo = List.of();
        }

        var options = dsLo.stream()
                .filter(l -> QuyTacTaiChe.MA_CHO_PHAN_LOAI.equals(l.getMatHang().getMaMatHang()))
                .map(l -> {
                    int dangCho = tongDangCho(l.getId(), -1L);
                    return DanhMucTaiCheResponse.LoOption.builder()
                            .id(l.getId()).maLo(l.getMaLo())
                            .tenMatHang(l.getMatHang().getTenMatHang())
                            .khoId(l.getKho().getId()).tenKho(l.getKho().getTenKho())
                            .tenNhaCungCap(l.getNcc() != null ? l.getNcc().getTenNcc() : null)
                            .ngayNhap(l.getNgayNhap()).donGiaNhap(l.getDonGiaNhap())
                            .soLuongNhap(l.getSoLuongNhap()).soLuongConLai(l.getSoLuongConLai())
                            .soLuongDangCho(dangCho)
                            .soLuongKhaDung(Math.max(0, l.getSoLuongConLai() - dangCho))
                            .build();
                })
                .toList();

        return DanhMucTaiCheResponse.builder()
                .khoMacDinhId(nv.getKho() != null ? nv.getKho().getId() : null)
                .loHang(options)
                .build();
    }

    // ======================= TẠO / SỬA / HỦY =======================

    @Override
    @Transactional
    public PhieuTaiCheResponse taoMoi(TaoPhieuTaiCheRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        LoHang lo = loHangRepository.findById(req.getLoHangId())
                .orElseThrow(() -> ResourceNotFoundException.of("Lô hàng", req.getLoHangId()));
        kiemTraLo(lo);
        phanQuyenKho.kiemTra(nv, lo.getKho());

        int kg = req.getKgPheLieuLoaiBo() == null ? 0 : req.getKgPheLieuLoaiBo();
        QuyTacTaiChe.kiemTraPhanLoai(req.getSoLuongXuLy(), req.getSoBanLai(), req.getSoThaoDo(),
                req.getSoLoaiBo(), kg);
        kiemTraConDuHang(lo, req.getSoLuongXuLy(), -1L);

        PhieuTaiChe p = phieuTaiCheRepository.save(PhieuTaiChe.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PTC", "PTC-"))
                .loHang(lo).kho(lo.getKho()).nhanVienThucHien(nv)
                .soLuongDaXuLy(req.getSoLuongXuLy())
                .soBanLai(req.getSoBanLai()).soThaoDo(req.getSoThaoDo()).soLoaiBo(req.getSoLoaiBo())
                .kgPheLieuLoaiBo(kg)
                .ngayThucHien(LocalDate.now())
                .trangThai(NHAP)
                .ghiChu(lamSach(req.getGhiChu()))
                .createdAt(LocalDateTime.now())
                .build());

        nhatKyService.ghi(nv, "tai_che", "tao_moi", "phieu_tai_che", p.getId());
        return toResponse(p, true);
    }

    @Override
    @Transactional
    public PhieuTaiCheResponse capNhat(Long id, TaoPhieuTaiCheRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuTaiChe p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ sửa được phiếu tái chế đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, p);

        LoHang lo = p.getLoHang();
        if (!lo.getId().equals(req.getLoHangId())) {
            throw new BusinessException("Không đổi được lô của phiếu, hãy hủy và tạo phiếu mới");
        }
        kiemTraLo(lo);

        int kg = req.getKgPheLieuLoaiBo() == null ? 0 : req.getKgPheLieuLoaiBo();
        QuyTacTaiChe.kiemTraPhanLoai(req.getSoLuongXuLy(), req.getSoBanLai(), req.getSoThaoDo(),
                req.getSoLoaiBo(), kg);
        kiemTraConDuHang(lo, req.getSoLuongXuLy(), p.getId());

        p.setSoLuongDaXuLy(req.getSoLuongXuLy());
        p.setSoBanLai(req.getSoBanLai());
        p.setSoThaoDo(req.getSoThaoDo());
        p.setSoLoaiBo(req.getSoLoaiBo());
        p.setKgPheLieuLoaiBo(kg);
        p.setGhiChu(lamSach(req.getGhiChu()));
        p = phieuTaiCheRepository.save(p);

        nhatKyService.ghi(nv, "tai_che", "cap_nhat", "phieu_tai_che", p.getId());
        return toResponse(p, true);
    }

    @Override
    @Transactional
    public PhieuTaiCheResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy phiếu tái chế");
        }
        PhieuTaiChe p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được phiếu tái chế đang ở trạng thái nháp");
        }
        kiemTraNguoiSua(nv, p);

        String ly = lyDo.trim();
        p.setTrangThai(DA_HUY);
        p.setLyDoHuy(ly.length() > 255 ? ly.substring(0, 255) : ly);
        p = phieuTaiCheRepository.save(p);

        nhatKyService.ghi(nv, "tai_che", "huy", "phieu_tai_che", p.getId());
        return toResponse(p, true);
    }

    // ======================= DUYỆT PHÂN LOẠI =======================

    @Override
    @Transactional
    public PhieuTaiCheResponse duyetPhanLoai(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuTaiChe p = khoaPhieu(id);
        if (!NHAP.equals(p.getTrangThai())) {
            throw new BusinessException("Phiếu không ở trạng thái nháp nên không duyệt được");
        }

        Long loHangId = p.getLoHang().getId();

        LoHang lo = loHangRepository.findForUpdate(loHangId)
                .orElseThrow(() -> ResourceNotFoundException.of("Lô hàng", loHangId));
        kiemTraLo(lo);

        int n = p.getSoLuongDaXuLy();
        int banLai = p.getSoBanLai();
        int thaoDo = p.getSoThaoDo();
        int loaiBo = p.getSoLoaiBo();
        int kg = p.getKgPheLieuLoaiBo();
        QuyTacTaiChe.kiemTraPhanLoai(n, banLai, thaoDo, loaiBo, kg); // kiểm tra lại phòng dữ liệu bị sửa ngoài luồng
        if (lo.getSoLuongConLai() < n) {
            throw new BusinessException("Lô " + lo.getMaLo() + " chỉ còn " + lo.getSoLuongConLai()
                    + " pallet, không đủ để duyệt phiếu " + p.getMaPhieu() + " (" + n + " pallet)");
        }

        Kho kho = p.getKho();
        MatHang nguon = lo.getMatHang();
        MatHang choThaoDo = layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO);
        MatHang datChuan = layMatHang(QuyTacTaiChe.MA_DAT_CHUAN);
        MatHang pheLieu = layMatHang(QuyTacTaiChe.MA_PHE_LIEU);

        // Cập nhật tồn theo thứ tự id mặt hàng tăng dần để tránh deadlock giữa các phiếu duyệt cùng lúc
        tonKhoNghiepVu.tru(nguon, kho, n);
        if (thaoDo > 0) tonKhoNghiepVu.cong(choThaoDo, kho, thaoDo);
        if (banLai > 0) tonKhoNghiepVu.cong(datChuan, kho, banLai);
        if (kg > 0) tonKhoNghiepVu.cong(pheLieu, kho, kg);

        // Phiếu xuất: pallet cũ chờ phân loại đi vào quá trình phân loại
        PhieuKho xuat = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PX", "PX-"))
                .loaiPhieu("xuat_kho").kho(kho).phieuTaiChe(p)
                .nhanVienGiaoNhan(p.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Xuất để phân loại theo phiếu tái chế " + p.getMaPhieu())
                .build());
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(xuat).matHang(nguon).lo(lo).soLuong(n)
                .ghiChu("Lô " + lo.getMaLo()).build());

        // Phiếu nhập: kết quả phân loại
        PhieuKho nhap = phieuKhoRepository.save(PhieuKho.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PN", "PN-"))
                .loaiPhieu("nhap_kho").kho(kho).phieuTaiChe(p)
                .nhanVienGiaoNhan(p.getNhanVienThucHien()).nguoiDuyet(nv)
                .ngayGio(LocalDateTime.now()).trangThai("da_hoan_thanh")
                .ghiChu("Nhập kết quả phân loại theo phiếu tái chế " + p.getMaPhieu())
                .build());

        List<ChiTietTaiChe> ketQua = new ArrayList<>();
        if (thaoDo > 0) {
            ghiDongNhap(nhap, choThaoDo, lo, thaoDo, "Pallet chờ tháo dỡ");
            ketQua.add(dongKetQua(p, choThaoDo, thaoDo, "Chờ tháo dỡ"));
        }
        if (banLai > 0) {
            ghiDongNhap(nhap, datChuan, lo, banLai, "Pallet đạt chuẩn bán nguyên trạng");
            ketQua.add(dongKetQua(p, datChuan, banLai, "Bán lại nguyên trạng"));
        }
        if (kg > 0) {
            ghiDongNhap(nhap, pheLieu, lo, kg, "Phế liệu từ pallet loại bỏ (kg)");
            ketQua.add(dongKetQua(p, pheLieu, kg, "Phế liệu từ " + loaiBo + " pallet loại bỏ (kg)"));
        }
        chiTietTaiCheRepository.saveAll(ketQua);

        lo.setSoLuongConLai(lo.getSoLuongConLai() - n);
        if (lo.getSoLuongConLai() == 0) lo.setTrangThai("da_het");
        loHangRepository.save(lo);

        p.setNguoiDuyet(nv);
        p.setThoiGianDuyet(LocalDateTime.now());
        p.setTrangThai(QuyTacTaiChe.trangThaiSauDuyet(thaoDo));
        p = phieuTaiCheRepository.save(p);

        nhatKyService.ghi(nv, "tai_che", "duyet_phan_loai", "phieu_tai_che", p.getId());
        return toResponse(p, true);
    }

    // ======================= HELPER =======================

    private void kiemTraLo(LoHang lo) {
        if (!"cho_tai_che".equals(lo.getMucDichNhap())) {
            throw new BusinessException("Lô " + lo.getMaLo() + " không dành cho tái chế");
        }
        if (!"con_hang".equals(lo.getTrangThai()) || lo.getSoLuongConLai() <= 0) {
            throw new BusinessException("Lô " + lo.getMaLo() + " đã xử lý hết");
        }
        if (!QuyTacTaiChe.MA_CHO_PHAN_LOAI.equals(lo.getMatHang().getMaMatHang())) {
            throw new BusinessException("Chỉ phân loại được lô \"Pallet gỗ cũ chờ phân loại\" ("
                    + QuyTacTaiChe.MA_CHO_PHAN_LOAI + ")");
        }
    }

    /** Số pallet của phiếu + các phiếu nháp khác của lô không được vượt số còn lại của lô. */
    private void kiemTraConDuHang(LoHang lo, int soXuLy, Long boQuaPhieuId) {
        int dangCho = tongDangCho(lo.getId(), boQuaPhieuId);
        int khaDung = lo.getSoLuongConLai() - dangCho;
        if (soXuLy > khaDung) {
            throw new BusinessException("Lô " + lo.getMaLo() + " còn " + lo.getSoLuongConLai()
                    + " pallet, trong đó " + dangCho + " đã nằm trong phiếu nháp khác. Chỉ còn " + Math.max(0, khaDung)
                    + " pallet để phân loại, không thể xử lý " + soXuLy);
        }
    }

    private int tongDangCho(Long loId, Long boQuaPhieuId) {
        Long v = phieuTaiCheRepository.tongDangCho(loId, boQuaPhieuId);
        return v == null ? 0 : v.intValue();
    }

    private void kiemTraNguoiSua(NhanVien nv, PhieuTaiChe p) {
        boolean laNguoiTao = p.getNhanVienThucHien().getId().equals(nv.getId());
        if (!phanQuyenKho.laQuanLy(nv) && !laNguoiTao) {
            throw new BusinessException("Chỉ người lập phiếu hoặc quản lý mới được sửa/hủy phiếu này");
        }
        phanQuyenKho.kiemTra(nv, p.getKho());
    }

    private void ghiDongNhap(PhieuKho nhap, MatHang mh, LoHang lo, int soLuong, String ghiChu) {
        chiTietPhieuKhoRepository.save(ChiTietPhieuKho.builder()
                .phieuKho(nhap).matHang(mh).lo(lo).soLuong(soLuong).ghiChu(ghiChu).build());
    }

    private ChiTietTaiChe dongKetQua(PhieuTaiChe p, MatHang mh, int soLuong, String ghiChu) {
        return ChiTietTaiChe.builder().phieuTaiChe(p).matHang(mh).soLuong(soLuong).ghiChu(ghiChu).build();
    }

    private MatHang layMatHang(String ma) {
        return matHangRepository.findByMaMatHang(ma)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng hệ thống mã " + ma
                        + ", hãy kiểm tra lại dữ liệu mặt hàng"));
    }

    private PhieuTaiChe timPhieu(Long id) {
        return phieuTaiCheRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu tái chế", id));
    }

    private PhieuTaiChe khoaPhieu(Long id) {
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

    private PhieuTaiCheResponse toResponse(PhieuTaiChe p, boolean chiTiet) {
        PhieuTaiCheResponse.PhieuTaiCheResponseBuilder b = PhieuTaiCheResponse.builder()
                .id(p.getId()).maPhieu(p.getMaPhieu()).trangThai(p.getTrangThai())
                .loHangId(p.getLoHang().getId()).maLo(p.getLoHang().getMaLo())
                .tenMatHang(p.getLoHang().getMatHang().getTenMatHang())
                .khoId(p.getKho().getId()).tenKho(p.getKho().getTenKho())
                .tenNguoiThucHien(p.getNhanVienThucHien().getHoTen())
                .tenNguoiDuyet(p.getNguoiDuyet() != null ? p.getNguoiDuyet().getHoTen() : null)
                .soLuongXuLy(p.getSoLuongDaXuLy()).soBanLai(p.getSoBanLai())
                .soThaoDo(p.getSoThaoDo()).soLoaiBo(p.getSoLoaiBo())
                .kgPheLieuLoaiBo(p.getKgPheLieuLoaiBo())
                .ngayThucHien(p.getNgayThucHien()).thoiGianDuyet(p.getThoiGianDuyet())
                .createdAt(p.getCreatedAt()).ghiChu(p.getGhiChu()).lyDoHuy(p.getLyDoHuy());

        if (chiTiet) {
            b.ketQua(chiTietTaiCheRepository.findByPhieuTaiCheId(p.getId()).stream()
                    .sorted(Comparator.comparing(ChiTietTaiChe::getId))
                    .map(c -> PhieuTaiCheResponse.KetQua.builder()
                            .matHangId(c.getMatHang().getId()).maMatHang(c.getMatHang().getMaMatHang())
                            .tenMatHang(c.getMatHang().getTenMatHang()).donViTinh(c.getMatHang().getDonViTinh())
                            .soLuong(c.getSoLuong()).ghiChu(c.getGhiChu()).build())
                    .toList());
            b.phieuKho(phieuKhoRepository.findByPhieuTaiChe_Id(p.getId()).stream()
                    .sorted(Comparator.comparing(PhieuKho::getId))
                    .map(k -> PhieuTaiCheResponse.PhieuKhoLienQuan.builder()
                            .id(k.getId()).maPhieu(k.getMaPhieu()).loaiPhieu(k.getLoaiPhieu())
                            .trangThai(k.getTrangThai()).build())
                    .toList());
        }
        return b.build();
    }
}