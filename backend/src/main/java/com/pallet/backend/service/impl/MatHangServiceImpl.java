package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.MatHangRequest;
import com.pallet.backend.dto.response.MatHangResponse;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhaCungCap;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.TonKho;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhaCungCapRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.service.MatHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatHangServiceImpl implements MatHangService {

    private final MatHangRepository matHangRepository;
    private final TonKhoRepository tonKhoRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MatHangResponse> layDanhSach(String trangThai) {
        List<MatHang> list = (trangThai != null)
                ? matHangRepository.findByTrangThai(trangThai)
                : matHangRepository.findAll();
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MatHangResponse layChiTiet(Long id) {
        MatHang mh = timHoacNem(id);
        return toResponse(mh);
    }

    @Override
    @Transactional
    public MatHangResponse taoMoi(MatHangRequest request) {
        MatHang mh = new MatHang();
        gan(mh, request);
        mh.setMaMatHang(sinhMaMatHang(request));
        mh.setTrangThai("dang_kinh_doanh");
        mh.setCreatedAt(LocalDateTime.now());
        mh.setUpdatedAt(LocalDateTime.now());
        mh.setCreatedBy(nhanVienDangDangNhap());
        mh = matHangRepository.save(mh);

        // Theo đúng luồng UI "Thêm pallet mới": nhập số lượng ban đầu -> tạo luôn dòng ton_kho.
        if (request.getSoLuongBanDau() != null) {
            if (request.getKhoId() == null) {
                throw new BusinessException("Phải chọn kho để nhập số lượng ban đầu");
            }
            var kho = khoRepository.findById(request.getKhoId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Kho", request.getKhoId()));
            TonKho tonKho = TonKho.builder()
                    .matHang(mh)
                    .kho(kho)
                    .soLuongTonKho(request.getSoLuongBanDau())
                    .soLuongDaGiuCho(0)
                    .soLuongToiThieu(0)
                    .updatedAt(LocalDateTime.now())
                    .build();
            tonKhoRepository.save(tonKho);
            // LƯU Ý: bước này CHỈ khởi tạo tồn kho, KHÔNG tự tạo phieu_kho/lo_hang tương ứng.
            // Nếu cần đầy đủ vết nhập kho (ai nhập, từ NCC nào), hãy tạo thêm PhieuKho +
            // ChiTietPhieuKho ở đây trong CÙNG transaction này.
        }

        return toResponse(mh);
    }

    @Override
    @Transactional
    public MatHangResponse capNhat(Long id, MatHangRequest request) {
        MatHang mh = timHoacNem(id);
        gan(mh, request);
        mh.setUpdatedAt(LocalDateTime.now());
        return toResponse(matHangRepository.save(mh));
    }

    @Override
    @Transactional
    public void ngungKinhDoanh(Long id) {
        MatHang mh = timHoacNem(id);
        // XÓA MỀM - đúng nguyên tắc đã thống nhất, KHÔNG bao giờ gọi matHangRepository.delete(...)
        mh.setTrangThai("ngung_kinh_doanh");
        mh.setUpdatedAt(LocalDateTime.now());
        matHangRepository.save(mh);
    }

    // ---------- helper ----------

    private MatHang timHoacNem(Long id) {
        return matHangRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Mặt hàng", id));
    }

    private void gan(MatHang mh, MatHangRequest r) {
        mh.setTenMatHang(r.getTenMatHang());
        mh.setLoaiMatHang(r.getLoaiMatHang());
        mh.setChatLieu(r.getChatLieu());
        mh.setKichThuocDai(r.getKichThuocDai());
        mh.setKichThuocRong(r.getKichThuocRong());
        mh.setKichThuocCao(r.getKichThuocCao());
        mh.setTaiTrongTinh(r.getTaiTrongTinh());
        mh.setTaiTrongDong(r.getTaiTrongDong());
        mh.setTieuChuan(r.getTieuChuan());
        mh.setDonGiaBan(r.getDonGiaBan());
        mh.setHinhAnh(r.getHinhAnh());
        mh.setMoTa(r.getMoTa());
        if (r.getNccMacDinhId() != null) {
            NhaCungCap ncc = nhaCungCapRepository.findById(r.getNccMacDinhId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Nhà cung cấp", r.getNccMacDinhId()));
            mh.setNccMacDinh(ncc);
        } else {
            mh.setNccMacDinh(null);
        }
    }

    private String sinhMaMatHang(MatHangRequest r) {
        String tienTo = "pallet".equals(r.getLoaiMatHang()) ? "PL" : "LK";
        String chatLieuVietTat = r.getChatLieu() != null
                ? r.getChatLieu().substring(0, Math.min(2, r.getChatLieu().length())).toUpperCase()
                : "XX";
        long stt = matHangRepository.count() + 1;
        return String.format("%s-%s-%02d", tienTo, chatLieuVietTat, stt);
    }

    private NhanVien nhanVienDangDangNhap() {
        String tenDangNhap = SecurityContextHolder.getContext().getAuthentication().getName();
        return nhanVienRepository.findByTenDangNhap(tenDangNhap).orElse(null);
    }

    private MatHangResponse toResponse(MatHang mh) {
        Integer tongTon = tonKhoRepository.sumSoLuongTonKho(mh.getId());
        Integer tongKhaDung = tonKhoRepository.sumSoLuongKhaDung(mh.getId());
        return MatHangResponse.builder()
                .id(mh.getId())
                .maMatHang(mh.getMaMatHang())
                .tenMatHang(mh.getTenMatHang())
                .loaiMatHang(mh.getLoaiMatHang())
                .chatLieu(mh.getChatLieu())
                .kichThuocDai(mh.getKichThuocDai())
                .kichThuocRong(mh.getKichThuocRong())
                .kichThuocCao(mh.getKichThuocCao())
                .taiTrongTinh(mh.getTaiTrongTinh())
                .taiTrongDong(mh.getTaiTrongDong())
                .tieuChuan(mh.getTieuChuan())
                .nccMacDinhTen(mh.getNccMacDinh() != null ? mh.getNccMacDinh().getTenNcc() : null)
                .donGiaBan(mh.getDonGiaBan())
                .hinhAnh(mh.getHinhAnh())
                .moTa(mh.getMoTa())
                .trangThai(mh.getTrangThai())
                .tongTonKho(tongTon)
                .tongKhaDung(tongKhaDung)
                .build();
    }
}
