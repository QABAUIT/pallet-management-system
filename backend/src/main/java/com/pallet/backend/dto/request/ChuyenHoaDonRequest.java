package com.pallet.backend.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChuyenHoaDonRequest {
    private Long khoId;               // bỏ trống = kho của nhân viên
    private LocalDate hanThanhToan;   // bỏ trống = 15 ngày
}