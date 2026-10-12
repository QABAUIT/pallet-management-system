package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThanhToanHoaDonRequest {

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String phuongThuc;

    private BigDecimal tienKhachDua;
    private String maGiaoDichNganHang;
}