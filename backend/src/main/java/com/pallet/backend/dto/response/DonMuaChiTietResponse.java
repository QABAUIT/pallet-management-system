package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DonMuaChiTietResponse {
    private Long id;
    private String maDonMua;
    private String trangThai;
    private boolean treHan;
    private LocalDate ngayDat;
    private LocalDate ngayDuKienNhan;
    private String ghiChu;
    private String lyDoHuy;
    private BigDecimal tongTien;
    private LocalDateTime createdAt;
    private Long khoId;
    private String tenKho;
    private DoiTac nhaCungCap;
    private String tenNhanVienLap;
    private String tenNguoiXacNhan;
    private List<Dong> chiTiet;
    private List<PhieuNhap> phieuNhap;

    @Data
    @Builder
    public static class DoiTac {
        private Long id;
        private String ma;
        private String ten;
        private String mst;
        private String sdt;
        private String diaChi;
        private String nguoiLienHe;
    }

    @Data
    @Builder
    public static class Dong {
        private Long id;            // id dòng đơn mua, dùng khi nhận hàng
        private int stt;
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private String loaiMatHang;
        private String donViTinh;
        private int soLuongDat;
        private int soLuongDaNhan;
        private int soLuongConLai;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
    }

    @Data
    @Builder
    public static class PhieuNhap {
        private Long id;
        private String maPhieu;
        private LocalDateTime ngayGio;
        private int tongSoLuong;
        private String trangThai;
    }
}