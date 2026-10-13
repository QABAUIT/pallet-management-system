package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChiPhiVanHanhResponse {
    private Long id;
    private String maChiPhi;
    private String loaiChiPhi;
    private String moTa;
    private BigDecimal soTien;
    private String ky;
    private LocalDate ngayChi;
    private String ghiChu;
    private Long khoId;
    private String tenKho;
    private String tenNguoiTao;
}