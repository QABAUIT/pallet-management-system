package com.pallet.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CapNhatHoaDonRequest {
    private LocalDate hanThanhToan;

    @Size(max = 2000, message = "Ghi chú không quá 2000 ký tự")
    private String ghiChu;
}