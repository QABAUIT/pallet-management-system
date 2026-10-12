package com.pallet.backend.service;

import com.pallet.backend.dto.request.KhachHangRequest;
import com.pallet.backend.dto.response.KhachHangResponse;
import com.pallet.backend.entity.HoaDon;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final HoaDonRepository hoaDonRepository;

    // Hóa đơn coi là "chưa thanh toán xong", tính vào công nợ.
    private static final Set<String> TRANG_THAI_CHUA_THANH_TOAN =
            Set.of("nhap", "cho_duyet", "qua_han");

    @Transactional(readOnly = true)
    public List<KhachHangResponse> layDanhSach(String trangThai) {
        List<KhachHang> list = (trangThai == null || trangThai.isBlank())
                ? khachHangRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : khachHangRepository.findByTrangThai(trangThai);

        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KhachHangResponse layChiTiet(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("khách hàng", id));
        return toResponse(kh);
    }

    @Transactional
    public KhachHangResponse taoMoi(KhachHangRequest req) {
        KhachHang kh = KhachHang.builder()
                .tenKh(req.getTenKh())
                .loaiKh(req.getLoaiKh() == null || req.getLoaiKh().isBlank() ? "doanh_nghiep" : req.getLoaiKh())
                .mst(req.getMst())
                .diaChi(req.getDiaChi())
                .sdt(req.getSdt())
                .email(req.getEmail())
                .nguoiDaiDien(req.getNguoiDaiDien())
                .trangThai(req.getTrangThai() == null || req.getTrangThai().isBlank() ? "dang_hop_tac" : req.getTrangThai())
                .createdAt(LocalDateTime.now())
                // TODO: gán createdBy = nhân viên đang đăng nhập khi có Spring Security
                .build();

        // ma_kh là NOT NULL UNIQUE ngay từ lúc insert, nhưng phải có id thật (do DB tự
        // sinh) mới đặt được mã đẹp dạng KH-00x -> lưu 2 bước: gán tạm 1 giá trị chắc
        // chắn duy nhất để qua được ràng buộc UNIQUE, sau khi có id thì sửa lại cho đúng.
        kh.setMaKh("TEMP-" + System.nanoTime());
        kh = khachHangRepository.saveAndFlush(kh);

        kh.setMaKh("KH-" + String.format("%03d", kh.getId()));
        kh = khachHangRepository.save(kh);

        return toResponse(kh);
    }

    @Transactional
    public KhachHangResponse capNhat(Long id, KhachHangRequest req) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("khách hàng", id));

        kh.setTenKh(req.getTenKh());
        if (req.getLoaiKh() != null && !req.getLoaiKh().isBlank()) {
            kh.setLoaiKh(req.getLoaiKh());
        }
        kh.setMst(req.getMst());
        kh.setDiaChi(req.getDiaChi());
        kh.setSdt(req.getSdt());
        kh.setEmail(req.getEmail());
        kh.setNguoiDaiDien(req.getNguoiDaiDien());
        if (req.getTrangThai() != null && !req.getTrangThai().isBlank()) {
            kh.setTrangThai(req.getTrangThai());
        }

        kh = khachHangRepository.save(kh);
        return toResponse(kh);
    }

    // ----- Tính toán tình trạng công nợ từ bảng hoa_don -----

    private KhachHangResponse toResponse(KhachHang kh) {
        List<HoaDon> hoaDonList = hoaDonRepository.findByKhachHang_Id(kh.getId());

        String tinhTrangCongNo;
        BigDecimal tongChiTieu = BigDecimal.ZERO;
        BigDecimal tongNo = BigDecimal.ZERO;

        if (hoaDonList.isEmpty()) {
            tinhTrangCongNo = "chua_phat_sinh";
        } else {
            boolean coQuaHan = hoaDonList.stream().anyMatch(h -> "qua_han".equals(h.getTrangThai()));
            boolean coChuaThanhToan = hoaDonList.stream()
                    .anyMatch(h -> TRANG_THAI_CHUA_THANH_TOAN.contains(h.getTrangThai()));

            if (coQuaHan) {
                tinhTrangCongNo = "qua_han";
            } else if (coChuaThanhToan) {
                tinhTrangCongNo = "dang_no";
            } else {
                tinhTrangCongNo = "tat_toan";
            }

            for (HoaDon h : hoaDonList) {
                BigDecimal tien = h.getTongThanhToan() == null ? BigDecimal.ZERO : h.getTongThanhToan();
                if ("da_thanh_toan".equals(h.getTrangThai())) {
                    tongChiTieu = tongChiTieu.add(tien);
                } else if (TRANG_THAI_CHUA_THANH_TOAN.contains(h.getTrangThai())) {
                    tongNo = tongNo.add(tien);
                }
            }
        }

        List<KhachHangResponse.HoaDonChuaThanhToanItem> chuaThanhToan = hoaDonList.stream()
                .filter(h -> TRANG_THAI_CHUA_THANH_TOAN.contains(h.getTrangThai()))
                .map(h -> KhachHangResponse.HoaDonChuaThanhToanItem.builder()
                        .maHoaDon(h.getMaHoaDon())
                        .ngayLap(h.getNgayLap())
                        .hanThanhToan(h.getHanThanhToan())
                        .tongThanhToan(h.getTongThanhToan())
                        .trangThai(h.getTrangThai())
                        .build())
                .collect(Collectors.toList());

        return KhachHangResponse.builder()
                .id(kh.getId())
                .maKh(kh.getMaKh())
                .tenKh(kh.getTenKh())
                .loaiKh(kh.getLoaiKh())
                .mst(kh.getMst())
                .diaChi(kh.getDiaChi())
                .sdt(kh.getSdt())
                .email(kh.getEmail())
                .nguoiDaiDien(kh.getNguoiDaiDien())
                .trangThai(kh.getTrangThai())
                .createdById(kh.getCreatedBy() != null ? kh.getCreatedBy().getId() : null)
                .tenNguoiTao(kh.getCreatedBy() != null ? kh.getCreatedBy().getHoTen() : null)
                .createdAt(kh.getCreatedAt())
                .tinhTrangCongNo(tinhTrangCongNo)
                .tongDonHang(hoaDonList.size())
                .tongChiTieu(tongChiTieu)
                .tongNo(tongNo)
                .hoaDonChuaThanhToan(chuaThanhToan)
                .build();
    }
}