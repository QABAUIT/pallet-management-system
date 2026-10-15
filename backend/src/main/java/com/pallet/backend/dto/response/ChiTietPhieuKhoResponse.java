package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChiTietPhieuKhoResponse {
    private Long matHangId;
    private String maMatHang;
    private String tenMatHang;
    private Integer soLuong;
    private String ghiChu;
    private Long loId; // Bổ sung trường này
}