package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PhieuXuLyThemResponse {
    private Long id;
    private String maPhieu;
    private String trangThai;
    private Long khoId;
    private String tenKho;
    private String loaiNguon;
    private String tenMatHangNguon;
    private String tenMatHangDich;
    private int soDatLai;
    private int soPheLieu;
    private int kgPheLieu;
    private String lyDo;
    private String tenNguoiThucHien;
    private String tenNguoiDuyet;
    private LocalDate ngayThucHien;
    private LocalDateTime thoiGianDuyet;
    private LocalDateTime createdAt;
    private String lyDoHuy;
}