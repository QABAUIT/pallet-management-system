package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BaoGiaResponse {
    private Long id;
    private String maBaoGia;
    private String trangThai;
    private Long khachHangId;
    private String tenKhachHang;
    private String tenNguoiTao;
    private LocalDate ngayTao;
    private LocalDate hanHieuLuc;
    private BigDecimal tyLeVat;
    private BigDecimal tongTienHang;
    private BigDecimal tienGiamGiaTong;
    private BigDecimal tienVat;
    private BigDecimal tongTien;
    private String ghiChu;
    private Long hoaDonId;
    private String maHoaDon;
    private String trangThaiHoaDon;
    private List<Dong> chiTiet;   // chỉ có ở màn chi tiết

    @Data
    @Builder
    public static class Dong {
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private int soLuong;
        private BigDecimal donGia;
        private BigDecimal tyLeChietKhau;
        private BigDecimal thanhTien;
    }
}