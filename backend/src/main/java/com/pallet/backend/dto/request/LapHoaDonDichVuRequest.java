package com.pallet.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LapHoaDonDichVuRequest {

    @DecimalMin(value = "0", message = "Thuế VAT không hợp lệ")
    @DecimalMax(value = "100", message = "Thuế VAT không hợp lệ")
    private BigDecimal tyLeVat; // bỏ trống = 8%

    private LocalDate hanThanhToan; // bỏ trống = 15 ngày

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;
}