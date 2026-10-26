package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DanhMucBanPheLieuResponse {
    private Long khoId;
    private String tenKho;
    private int tonKhaDungKg;
    private BigDecimal donGiaKgGoiY;
    private List<DanhMucBanHangResponse.KhachHangOption> khachHang;
}