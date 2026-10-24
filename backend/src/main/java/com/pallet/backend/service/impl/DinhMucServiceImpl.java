package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.TaoDinhMucRequest;
import com.pallet.backend.dto.response.DinhMucResponse;
import com.pallet.backend.entity.ChiTietDinhMuc;
import com.pallet.backend.entity.DinhMuc;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.ChiTietDinhMucRepository;
import com.pallet.backend.repository.DinhMucRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.service.DinhMucService;
import com.pallet.backend.service.NhatKyService;
import com.pallet.backend.service.QuyTacDongPallet;
import com.pallet.backend.service.QuyTacTaiChe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DinhMucServiceImpl implements DinhMucService {

    private final DinhMucRepository dinhMucRepository;
    private final ChiTietDinhMucRepository chiTietDinhMucRepository;
    private final MatHangRepository matHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final NhatKyService nhatKyService;

    @Override
    @Transactional(readOnly = true)
    public List<DinhMucResponse> layDanhSach(boolean chiHoatDong) {
        List<DinhMuc> ds = chiHoatDong ? dinhMucRepository.timHoatDong() : dinhMucRepository.timTatCa();
        return ds.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DinhMucResponse layChiTiet(Long id) {
        return toResponse(timDinhMuc(id));
    }

    @Override
    @Transactional
    public DinhMucResponse taoMoi(TaoDinhMucRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        MatHang thanhPham = kiemTraThanhPham(req.getThanhPhamId());
        List<ChiTietDinhMuc> dong = dungDong(req, null);

        DinhMuc dm = dinhMucRepository.save(DinhMuc.builder()
                .maDinhMuc(phieuKhoRepository.getMaChungTu("PTC", "DM-"))
                .tenDinhMuc(req.getTenDinhMuc().trim())
                .matHangThanhPham(thanhPham).trangThai("hoat_dong")
                .ghiChu(lamSach(req.getGhiChu())).createdAt(LocalDateTime.now())
                .build());
        dong.forEach(c -> c.setDinhMuc(dm));
        chiTietDinhMucRepository.saveAll(dong);

        nhatKyService.ghi(nv, "dinh_muc", "tao_moi", "dinh_muc", dm.getId());
        return toResponse(dm);
    }

    @Override
    @Transactional
    public DinhMucResponse capNhat(Long id, TaoDinhMucRequest req, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        DinhMuc dm = dinhMucRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Định mức", id));
        MatHang thanhPham = kiemTraThanhPham(req.getThanhPhamId());
        List<ChiTietDinhMuc> dong = dungDong(req, thanhPham.getId());

        dm.setTenDinhMuc(req.getTenDinhMuc().trim());
        dm.setMatHangThanhPham(thanhPham);
        dm.setGhiChu(lamSach(req.getGhiChu()));
        dm = dinhMucRepository.save(dm);

        // Phiếu đóng pallet cũ đã lưu số định mức riêng nên sửa định mức không làm đổi lịch sử
        chiTietDinhMucRepository.xoaTheoDinhMuc(dm.getId());
        final DinhMuc dmLuu = dm;
        dong.forEach(c -> c.setDinhMuc(dmLuu));
        chiTietDinhMucRepository.saveAll(dong);

        nhatKyService.ghi(nv, "dinh_muc", "cap_nhat", "dinh_muc", dm.getId());
        return toResponse(dm);
    }

    @Override
    @Transactional
    public DinhMucResponse doiTrangThai(Long id, boolean hoatDong, String tenDangNhap) {
        NhanVien nv = timNhanVien(tenDangNhap);
        DinhMuc dm = dinhMucRepository.findForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Định mức", id));
        dm.setTrangThai(hoatDong ? "hoat_dong" : "ngung");
        dm = dinhMucRepository.save(dm);
        nhatKyService.ghi(nv, "dinh_muc", hoatDong ? "kich_hoat" : "ngung", "dinh_muc", dm.getId());
        return toResponse(dm);
    }

    // ---------- helper ----------

    private MatHang kiemTraThanhPham(Long id) {
        MatHang mh = matHangRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Mặt hàng", id));
        if ("kg".equals(mh.getDonViTinh())) {
            throw new BusinessException("Thành phẩm không thể là mặt hàng tính theo kg (phế liệu)");
        }
        String ma = mh.getMaMatHang();
        if (QuyTacTaiChe.MA_CHO_PHAN_LOAI.equals(ma) || QuyTacTaiChe.MA_CHO_THAO_DO.equals(ma)) {
            throw new BusinessException("Thành phẩm không thể là pallet cũ chờ phân loại/tháo dỡ");
        }
        return mh;
    }

    /** Kiểm tra và dựng các dòng chi tiết (chưa gắn định mức). */
    private List<ChiTietDinhMuc> dungDong(TaoDinhMucRequest req, Long thanhPhamIdDaKiemTra) {
        List<Long> ids = req.getChiTiet().stream().map(TaoDinhMucRequest.Dong::getMatHangId).toList();
        List<Integer> sls = req.getChiTiet().stream().map(TaoDinhMucRequest.Dong::getSoLuong).toList();
        QuyTacDongPallet.kiemTraDinhMuc(ids, sls);

        List<ChiTietDinhMuc> dong = new ArrayList<>();
        for (TaoDinhMucRequest.Dong d : req.getChiTiet()) {
            MatHang vt = matHangRepository.findById(d.getMatHangId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vật tư", d.getMatHangId()));
            kiemTraVatTu(vt, req.getThanhPhamId());
            dong.add(ChiTietDinhMuc.builder().matHang(vt).soLuong(d.getSoLuong()).build());
        }
        return dong;
    }

    static void kiemTraVatTu(MatHang vt, Long thanhPhamId) {
        String ma = vt.getMaMatHang();
        if ("kg".equals(vt.getDonViTinh())) {
            throw new BusinessException("Vật tư " + ma + " tính theo kg (phế liệu), không dùng được trong định mức");
        }
        if (QuyTacTaiChe.MA_CHO_PHAN_LOAI.equals(ma) || QuyTacTaiChe.MA_CHO_THAO_DO.equals(ma)) {
            throw new BusinessException("Pallet cũ chờ phân loại/tháo dỡ không phải vật tư đóng pallet");
        }
        if (vt.getId().equals(thanhPhamId)) {
            throw new BusinessException("Thành phẩm không thể tự là vật tư của chính nó");
        }
    }

    private DinhMuc timDinhMuc(Long id) {
        return dinhMucRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Định mức", id));
    }

    private NhanVien timNhanVien(String tenDangNhap) {
        return nhanVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không xác định được người dùng đang đăng nhập"));
    }

    private String lamSach(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private DinhMucResponse toResponse(DinhMuc d) {
        return DinhMucResponse.builder()
                .id(d.getId()).maDinhMuc(d.getMaDinhMuc()).tenDinhMuc(d.getTenDinhMuc())
                .trangThai(d.getTrangThai()).ghiChu(d.getGhiChu())
                .thanhPhamId(d.getMatHangThanhPham().getId())
                .maThanhPham(d.getMatHangThanhPham().getMaMatHang())
                .tenThanhPham(d.getMatHangThanhPham().getTenMatHang())
                .chiTiet(chiTietDinhMucRepository.timTheoDinhMuc(d.getId()).stream()
                        .map(c -> DinhMucResponse.Dong.builder()
                                .matHangId(c.getMatHang().getId()).maMatHang(c.getMatHang().getMaMatHang())
                                .tenMatHang(c.getMatHang().getTenMatHang()).soLuong(c.getSoLuong()).build())
                        .toList())
                .build();
    }
}