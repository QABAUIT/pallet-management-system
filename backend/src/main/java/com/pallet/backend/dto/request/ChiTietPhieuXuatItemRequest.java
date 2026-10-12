package com.pallet.backend.dto.request;

import lombok.Data;

@Data
public class ChiTietPhieuXuatItemRequest {
    private Long matHangId;
    private Long loId; // Tùy chọn: ID của lô hàng xuất (để trừ đúng lô)
    private Integer soLuong;
    private String ghiChu;
}