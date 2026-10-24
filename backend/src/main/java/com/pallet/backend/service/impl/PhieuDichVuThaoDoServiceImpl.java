package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.KetQuaDichVuRequest;
import com.pallet.backend.dto.request.TiepNhanDichVuRequest;
import com.pallet.backend.dto.response.PhieuDichVuThaoDoResponse;
import com.pallet.backend.dto.response.TonKhoKhachResponse;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuDichVuThaoDo;
import com.pallet.backend.entity.TonKhoKhach;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.KhachHangRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuDichVuThaoDoRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.TonKhoKhachRepository;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.PhanQuyenKho;
import com.pallet.backend.service.PhieuDichVuThaoDoService;
import com.pallet.backend.service.QuyTacDichVuThaoDo;
import com.pallet.backend.service.QuyTacTaiChe;
import com.pallet.backend.service.TonKhoKhachNghiepVu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuDichVuThaoDoServiceImpl implements PhieuDichVuThaoDoService {

    private static final String DA_TIEP_NHAN = "da_tiep_nhan";
    private static final String CHO_DUYET = "cho_duyet";
    private static final String DA_THAO_DO = "da_thao_do";
    private static final String DA_TRA_HANG = "da_tra_hang";
    private static final String DA_HUY = "da_huy";

    private final PhieuDichVuThaoDoRepository phieuRepository;
    private final TonKhoKhachRepository tonKhoKhachRepository;
    private final KhachHangRepository khachHangRepository;
    private final KhoRepository khoRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final TonKhoKhachNghiepVu tonKhoKhachNghiepVu;
    private final PhanQuyenKho phanQuyenKho;
    private final NhatKyService nhatKyService;

    // ======================= ĐỌC =======================

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDichVuThaoDoResponse> layDanhSach(String trangThai, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<PhieuDichVuThaoDo> ds = (trangThai == null || trangThai.isBlank())
                ? phieuRepository.timTatCa() : phieuRepository.timTheoTrangThai(trangThai);
        return ds.stream().filter(p -> phanQuyenKho.duocThaoTac(nv, p.getKho())).map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuDichVuThaoDoResponse layChiTiet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = timPhieu(id);
        phanQuyenKho.kiemTra(nv, p.getKho());
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public TonKhoKhachResponse layTonKhach(Long khachHangId, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        List<TonKhoKhach> ds = khachHangId == null
                ? tonKhoKhachRepository.timTatCaCoTon() : tonKhoKhachRepository.timTheoKhach(khachHangId);
        return TonKhoKhachResponse.builder().dong(ds.stream()
                .filter(t -> phanQuyenKho.duocThaoTac(nv, t.getKho()))
                .map(t -> TonKhoKhachResponse.Dong.builder()
                        .khachHangId(t.getKhachHang().getId()).tenKhachHang(t.getKhachHang().getTenKh())
                        .khoId(t.getKho().getId()).tenKho(t.getKho().getTenKho())
                        .matHangId(t.getMatHang().getId()).maMatHang(t.getMatHang().getMaMatHang())
                        .tenMatHang(t.getMatHang().getTenMatHang()).donViTinh(t.getMatHang().getDonViTinh())
                        .soLuong(t.getSoLuong()).build())
                .toList()).build();
    }

    // ======================= TIẾP NHẬN =======================

    @Override
    @Transactional
    public PhieuDichVuThaoDoResponse tiepNhan(TiepNhanDichVuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        Kho kho = khoRepository.findById(req.getKhoId())
                .orElseThrow(() -> ResourceNotFoundException.of("Kho", req.getKhoId()));
        phanQuyenKho.kiemTra(nv, kho);
        KhachHang khach = khachHangRepository.findById(req.getKhachHangId())
                .orElseThrow(() -> ResourceNotFoundException.of("Khách hàng", req.getKhachHangId()));
        QuyTacDichVuThaoDo.kiemTraTiepNhan(req.getSoPalletNhan(), req.getDonGiaDichVu());

        PhieuDichVuThaoDo p = phieuRepository.save(PhieuDichVuThaoDo.builder()
                .maPhieu(phieuKhoRepository.getMaChungTu("PTC", "DV-"))
                .khachHang(khach).kho(kho).nhanVienTiepNhan(nv)
                .soPalletNhan(req.getSoPalletNhan()).donGiaDichVu(req.getDonGiaDichVu())
                .ngayTiepNhan(LocalDate.now()).trangThai(DA_TIEP_NHAN)
                .ghiChu(lamSach(req.getGhiChu())).createdAt(LocalDateTime.now())
                .build());

        // Pallet của khách vào SỔ KHÁCH, không vào ton_kho công ty (TC08)
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO), kho, p.getSoPalletNhan());

        nhatKyService.ghi(nv, "dich_vu_thao_do", "tiep_nhan", "phieu_dich_vu_thao_do", p.getId());
        return toResponse(p);
    }

    // ======================= GHI NHẬN KẾT QUẢ / DUYỆT =======================

    @Override
    @Transactional
    public PhieuDichVuThaoDoResponse ghiNhanKetQua(Long id, KetQuaDichVuRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = khoaPhieu(id);
        if (!DA_TIEP_NHAN.equals(p.getTrangThai()) && !CHO_DUYET.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ ghi nhận kết quả khi phiếu đang tiếp nhận hoặc chờ duyệt");
        }
        phanQuyenKho.kiemTra(nv, p.getKho());

        int tn = z(req.getSoThanhNguyen()), cv = z(req.getSoCucVuong());
        int tx = z(req.getSoThanhCanXuLy()), cx = z(req.getSoCucCanXuLy()), kg = z(req.getKgPheLieu());
        QuyTacDichVuThaoDo.kiemTraKetQua(p.getSoPalletNhan(), req.getSoPalletThaoDo(), tn, cv, tx, cx, kg);

        p.setSoPalletThaoDo(req.getSoPalletThaoDo());
        p.setSoThanhNguyen(tn);
        p.setSoCucVuong(cv);
        p.setSoThanhCanXuLy(tx);
        p.setSoCucCanXuLy(cx);
        p.setKgPheLieu(kg);
        p.setTrangThai(CHO_DUYET);
        p = phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "ghi_nhan_ket_qua", "phieu_dich_vu_thao_do", p.getId());
        return toResponse(p);
    }

    @Override
    @Transactional
    public PhieuDichVuThaoDoResponse duyet(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = khoaPhieu(id);
        if (!CHO_DUYET.equals(p.getTrangThai())) {
            throw new BusinessException("Phiếu chưa có kết quả tháo dỡ chờ duyệt");
        }
        QuyTacDichVuThaoDo.kiemTraKetQua(p.getSoPalletNhan(), p.getSoPalletThaoDo(), p.getSoThanhNguyen(),
                p.getSoCucVuong(), p.getSoThanhCanXuLy(), p.getSoCucCanXuLy(), p.getKgPheLieu());

        KhachHang khach = p.getKhachHang();
        Kho kho = p.getKho();
        // Pallet đã tháo dỡ biến thành linh kiện/phế liệu, vẫn thuộc sổ của khách
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO), kho, p.getSoPalletThaoDo());
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_THANH_NGUYEN), kho, p.getSoThanhNguyen());
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_CUC_VUONG), kho, p.getSoCucVuong());
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_THANH_CAN_XU_LY), kho, p.getSoThanhCanXuLy());
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_CUC_CAN_XU_LY), kho, p.getSoCucCanXuLy());
        tonKhoKhachNghiepVu.cong(khach, layMatHang(QuyTacTaiChe.MA_PHE_LIEU), kho, p.getKgPheLieu());

        p.setPhiDichVu(QuyTacDichVuThaoDo.tinhPhi(p.getSoPalletThaoDo(), p.getDonGiaDichVu()));
        p.setNguoiDuyet(nv);
        p.setThoiGianDuyet(LocalDateTime.now());
        p.setTrangThai(DA_THAO_DO);
        p = phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "duyet", "phieu_dich_vu_thao_do", p.getId());
        return toResponse(p);
    }

    // ======================= TRẢ HÀNG / HỦY =======================

    @Override
    @Transactional
    public PhieuDichVuThaoDoResponse traHang(Long id, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        PhieuDichVuThaoDo p = khoaPhieu(id);
        if (!DA_THAO_DO.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ trả hàng sau khi kết quả tháo dỡ đã được duyệt");
        }
        phanQuyenKho.kiemTra(nv, p.getKho());

        KhachHang khach = p.getKhachHang();
        Kho kho = p.getKho();
        // Trả đúng những gì đã ghi sổ cho khách: pallet chưa tháo, linh kiện, phế liệu
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO), kho, p.getSoPalletNhan() - p.getSoPalletThaoDo());
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_THANH_NGUYEN), kho, p.getSoThanhNguyen());
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_CUC_VUONG), kho, p.getSoCucVuong());
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_THANH_CAN_XU_LY), kho, p.getSoThanhCanXuLy());
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_CUC_CAN_XU_LY), kho, p.getSoCucCanXuLy());
        tonKhoKhachNghiepVu.tru(khach, layMatHang(QuyTacTaiChe.MA_PHE_LIEU), kho, p.getKgPheLieu());

        p.setThoiGianTraHang(LocalDateTime.now());
        p.setTrangThai(DA_TRA_HANG);
        p = phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "tra_hang", "phieu_dich_vu_thao_do", p.getId());
        return toResponse(p);
    }

    @Override
    @Transactional
    public PhieuDichVuThaoDoResponse huy(Long id, String lyDo, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Bắt buộc nhập lý do hủy phiếu dịch vụ");
        }
        PhieuDichVuThaoDo p = khoaPhieu(id);
        if (!DA_TIEP_NHAN.equals(p.getTrangThai()) && !CHO_DUYET.equals(p.getTrangThai())) {
            throw new BusinessException("Chỉ hủy được phiếu trước khi duyệt kết quả tháo dỡ");
        }
        phanQuyenKho.kiemTra(nv, p.getKho());

        // Hoàn lại pallet đã nhận vào sổ khách (trả khách nguyên trạng)
        tonKhoKhachNghiepVu.tru(p.getKhachHang(), layMatHang(QuyTacTaiChe.MA_CHO_THAO_DO), p.getKho(), p.getSoPalletNhan());

        String ly = lyDo.trim();
        p.setTrangThai(DA_HUY);
        p.setLyDoHuy(ly.length() > 255 ? ly.substring(0, 255) : ly);
        p = phieuRepository.save(p);

        nhatKyService.ghi(nv, "dich_vu_thao_do", "huy", "phieu_dich_vu_thao_do", p.getId());
        return toResponse(p);
    }

    // ======================= HELPER =======================

    private int z(Integer v) {
        return v == null ? 0 : v;
    }

    private MatHang layMatHang(String ma) {
        return matHangRepository.findByMaMatHang(ma)
                .orElseThrow(() -> new BusinessException("Thiếu mặt hàng hệ thống mã " + ma));
    }

    private PhieuDichVuThaoDo timPhieu(Long id) {
        return phieuRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu dịch vụ tháo dỡ", id));
    }

    private PhieuDichVuThaoDo khoaPhieu(Long id) {
        return phieuRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Phiếu dịch vụ tháo dỡ", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private PhieuDichVuThaoDoResponse toResponse(PhieuDichVuThaoDo p) {
        boolean daCoKetQua = !DA_TIEP_NHAN.equals(p.getTrangThai()) && !DA_HUY.equals(p.getTrangThai());
        return PhieuDichVuThaoDoResponse.builder()
                .id(p.getId()).maPhieu(p.getMaPhieu()).trangThai(p.getTrangThai())
                .khachHangId(p.getKhachHang().getId()).tenKhachHang(p.getKhachHang().getTenKh())
                .khoId(p.getKho().getId()).tenKho(p.getKho().getTenKho())
                .tenNguoiTiepNhan(p.getNhanVienTiepNhan().getHoTen())
                .tenNguoiDuyet(p.getNguoiDuyet() != null ? p.getNguoiDuyet().getHoTen() : null)
                .soPalletNhan(p.getSoPalletNhan()).donGiaDichVu(p.getDonGiaDichVu())
                .soPalletThaoDo(p.getSoPalletThaoDo())
                .soPalletTraNguyen(daCoKetQua ? p.getSoPalletNhan() - p.getSoPalletThaoDo() : 0)
                .soThanhNguyen(p.getSoThanhNguyen()).soCucVuong(p.getSoCucVuong())
                .soThanhCanXuLy(p.getSoThanhCanXuLy()).soCucCanXuLy(p.getSoCucCanXuLy())
                .kgPheLieu(p.getKgPheLieu()).phiDichVu(p.getPhiDichVu())
                .ngayTiepNhan(p.getNgayTiepNhan()).thoiGianDuyet(p.getThoiGianDuyet())
                .thoiGianTraHang(p.getThoiGianTraHang()).createdAt(p.getCreatedAt())
                .ghiChu(p.getGhiChu()).lyDoHuy(p.getLyDoHuy())
                .build();
    }
}