package com.pallet.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MucTieuDoanhThuRequest {

    @NotBlank(message = "Vui lòng chọn kỳ")
    @Pattern(regexp = "^(thang|nam)$", message = "Kỳ phải là tháng hoặc năm")
    private String ky;

    @NotNull(message = "Vui lòng nhập năm")
    @Min(value = 2000, message = "Năm không hợp lệ")
    @Max(value = 2100, message = "Năm không hợp lệ")
    private Integer namKy;

    @Min(value = 1, message = "Tháng không hợp lệ")
    @Max(value = 12, message = "Tháng không hợp lệ")
    private Integer thangKy; // bắt buộc khi ky = thang

    @NotNull(message = "Vui lòng nhập số tiền mục tiêu")
    @DecimalMin(value = "0", message = "Số tiền mục tiêu không hợp lệ")
    private BigDecimal soTienMucTieu;

    @Size(max = 255, message = "Ghi chú không quá 255 ký tự")
    private String ghiChu;
}