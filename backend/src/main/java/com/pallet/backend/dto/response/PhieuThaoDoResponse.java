package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PhieuThaoDoResponse {
    private Long id;
    private String maPhieu;
    private String trangThai;
    private Long phieuTaiCheId;
    private String maPhieuTaiChe;
    private String maLo;
    private Long khoId;
    private String tenKho;
    private String tenNguoiThucHien;
    private String tenNguoiDuyet;
    private int soPalletThaoDo;
    private int soThanhNguyen;
    private int soCucVuong;
    private int soThanhCanXuLy;
    private int soCucCanXuLy;
    private int kgPheLieu;
    private LocalDate ngayThucHien;
    private LocalDateTime thoiGianDuyet;
    private LocalDateTime createdAt;
    private String ghiChu;
    private String lyDoHuy;
}