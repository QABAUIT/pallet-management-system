package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MucTieuDoanhThuResponse {
    private Long id;
    private String ky; // thang | nam
    private Integer namKy;
    private Short thangKy;
    private BigDecimal soTienMucTieu;
    private String ghiChu;
    private BigDecimal doanhThuThucTe;
    private Double phanTramDat;
}