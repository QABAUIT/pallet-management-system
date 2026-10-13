package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BaoCaoDoanhThuResponse {
    private BigDecimal doanhThu;
    private Double phanTramDoanhThu;   // so với kỳ trước, null nếu kỳ trước = 0
    private Long soHoaDon;
    private Double phanTramSoHoaDon;
    private Long tonKhoPallet;
    private Long daGiuCho;
    private Long tongKhachHang;
    private Long khachHangMoi;

    private MucTieu mucTieu;           // null nếu chưa đặt hoặc khung thời gian không phải 1 tháng / 1 năm
    private List<DiemThang> bieuDoThang;
    private List<CoCau> coCauPallet;
    private String sanPhamBanChayNhat;
    private List<GiaoDich> giaoDichGanDay;

    @Data
    @Builder
    public static class MucTieu {
        private Long id;
        private String ky;
        private Integer namKy;
        private Short thangKy;
        private BigDecimal soTien;
        private Double phanTramDat;
    }

    @Data
    @Builder
    public static class DiemThang {
        private String nhan;
        private BigDecimal doanhThu;
    }

    @Data
    @Builder
    public static class CoCau {
        private String ten;
        private BigDecimal soTien;
        private Long soLuong;
        private Double phanTram;
    }

    @Data
    @Builder
    public static class GiaoDich {
        private Long id;
        private String maHoaDon;
        private String tenKhachHang;
        private String mstKhachHang;
        private LocalDateTime ngayLap;
        private String tenMatHang;     // mặt hàng đầu tiên của hóa đơn
        private Integer soLuong;
        private Integer soLoaiKhac;    // số loại hàng còn lại trong hóa đơn
        private BigDecimal donGia;
        private BigDecimal tongThanhToan;
        private String trangThai;
        private boolean quaHan;
    }
}