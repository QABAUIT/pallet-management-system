package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TiepNhanDichVuRequest {

    @NotNull(message = "Chưa chọn khách hàng")
    private Long khachHangId;

    @NotNull(message = "Chưa chọn kho")
    private Long khoId;

    @NotNull(message = "Chưa nhập số pallet nhận")
    @Positive(message = "Số pallet nhận phải lớn hơn 0")
    private Integer soPalletNhan;

    @NotNull(message = "Chưa nhập đơn giá dịch vụ")
    @PositiveOrZero(message = "Đơn giá dịch vụ phải >= 0")
    private BigDecimal donGiaDichVu;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;
}