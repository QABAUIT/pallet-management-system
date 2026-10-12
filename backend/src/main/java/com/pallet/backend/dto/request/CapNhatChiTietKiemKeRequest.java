package com.pallet.backend.dto.request;

import lombok.Data;

@Data
public class CapNhatChiTietKiemKeRequest {
    private Integer tonThucTe;
    private String ghiChuGiaiTrinh;
    private Long nguoiKiemId;
}