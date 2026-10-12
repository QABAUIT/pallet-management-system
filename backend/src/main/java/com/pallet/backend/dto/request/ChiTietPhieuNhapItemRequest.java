package com.pallet.backend.dto.request;

import lombok.Data;

@Data
public class ChiTietPhieuNhapItemRequest {
    private Long matHangId;
    private Integer soLuong;
    private String ghiChu;
}