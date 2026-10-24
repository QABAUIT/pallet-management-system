package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PhieuDichVuThaoDoResponse {
    private Long id;
    private String maPhieu;
    private String trangThai;
    private Long khachHangId;
    private String tenKhachHang;
    private Long khoId;
    private String tenKho;
    private String tenNguoiTiepNhan;
    private String tenNguoiDuyet;
    private int soPalletNhan;
    private BigDecimal donGiaDichVu;
    private int soPalletThaoDo;
    private int soPalletTraNguyen;     // nhận - tháo dỡ, trả khách nguyên trạng
    private int soThanhNguyen;
    private int soCucVuong;
    private int soThanhCanXuLy;
    private int soCucCanXuLy;
    private int kgPheLieu;
    private BigDecimal phiDichVu;
    private LocalDate ngayTiepNhan;
    private LocalDateTime thoiGianDuyet;
    private LocalDateTime thoiGianTraHang;
    private LocalDateTime createdAt;
    private String ghiChu;
    private String lyDoHuy;
}