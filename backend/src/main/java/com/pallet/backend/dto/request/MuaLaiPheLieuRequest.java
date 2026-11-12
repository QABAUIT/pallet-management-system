package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MuaLaiPheLieuRequest {

    @NotNull(message = "Chưa nhập số kg mua lại")
    @Positive(message = "Số kg mua lại phải lớn hơn 0")
    private Integer soKg;

    @NotNull(message = "Chưa nhập đơn giá mỗi kg")
    @Positive(message = "Đơn giá mỗi kg phải lớn hơn 0")
    private BigDecimal donGiaKg;

    @NotBlank(message = "Chưa chọn phương thức thanh toán")
    private String phuongThucThanhToan; // tien_mat | chuyen_khoan | qr_code

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;
}