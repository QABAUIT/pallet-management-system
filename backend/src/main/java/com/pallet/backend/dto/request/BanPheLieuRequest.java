package com.pallet.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BanPheLieuRequest {

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Long khachHangId;

    private Long khoId; // bỏ trống = kho của nhân viên

    @NotNull(message = "Chưa nhập số kg")
    @Positive(message = "Số kg phải lớn hơn 0")
    private Integer soKg;

    @DecimalMin(value = "0", inclusive = false, message = "Đơn giá mỗi kg phải lớn hơn 0")
    private BigDecimal donGiaKg; // bỏ trống = giá bán hiện hành của phế liệu

    @DecimalMin(value = "0", message = "Thuế VAT không hợp lệ")
    @DecimalMax(value = "100", message = "Thuế VAT không hợp lệ")
    private BigDecimal tyLeVat; // bỏ trống = 8%

    private LocalDate hanThanhToan;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    private boolean thanhToanNgay;
    private String phuongThucThanhToan; // tien_mat | chuyen_khoan | qr_code
    private BigDecimal tienKhachDua;
}