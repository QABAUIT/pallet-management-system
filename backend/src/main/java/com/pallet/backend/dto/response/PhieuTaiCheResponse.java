package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PhieuTaiCheResponse {
    private Long id;
    private String maPhieu;
    private String trangThai;
    private Long loHangId;
    private String maLo;
    private String tenMatHang;
    private Long khoId;
    private String tenKho;
    private String tenNguoiThucHien;
    private String tenNguoiDuyet;
    private int soLuongXuLy;
    private int soBanLai;
    private int soThaoDo;
    private int soLoaiBo;
    private int kgPheLieuLoaiBo;
    private LocalDate ngayThucHien;
    private LocalDateTime thoiGianDuyet;
    private LocalDateTime createdAt;
    private String ghiChu;
    private String lyDoHuy;

    // chỉ có ở màn chi tiết
    private List<KetQua> ketQua;
    private List<PhieuKhoLienQuan> phieuKho;

    @Data
    @Builder
    public static class KetQua {
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private String donViTinh;
        private int soLuong;
        private String ghiChu;
    }

    @Data
    @Builder
    public static class PhieuKhoLienQuan {
        private Long id;
        private String maPhieu;
        private String loaiPhieu;
        private String trangThai;
    }
}