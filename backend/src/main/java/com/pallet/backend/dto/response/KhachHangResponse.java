package com.pallet.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangResponse {

    private Long id;
    private String maKh;
    private String tenKh;
    private String loaiKh;
    private String mst;
    private String diaChi;
    private String sdt;
    private String email;
    private String nguoiDaiDien;

    // Trạng thái hợp tác (field tĩnh lưu sẵn trong bảng khach_hang)
    private String trangThai; // dang_hop_tac | ngung_hop_tac

    private Long createdById;
    private String tenNguoiTao;
    private LocalDateTime createdAt;

    // ----- Các field TÍNH TOÁN từ bảng hoa_don, không lưu sẵn trong DB -----

    /** tat_toan | dang_no | qua_han | chua_phat_sinh */
    private String tinhTrangCongNo;

    private Integer tongDonHang;
    private BigDecimal tongChiTieu; // tổng tiền các hóa đơn đã thanh toán
    private BigDecimal tongNo;      // tổng tiền các hóa đơn chưa thanh toán xong

    private List<HoaDonChuaThanhToanItem> hoaDonChuaThanhToan;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoaDonChuaThanhToanItem {
        private String maHoaDon;
        private LocalDateTime ngayLap;
        private LocalDate hanThanhToan;
        private BigDecimal tongThanhToan;
        private String trangThai; // nhap | cho_duyet | qua_han
    }
}