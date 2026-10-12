package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Trường nào để null thì giữ nguyên. Hóa đơn đã thanh toán chỉ được sửa ghiChu. */
@Data
public class CapNhatHoaDonRequest {

    private Long khachHangId;

    @Valid
    private List<TaoHoaDonRequest.DongHang> chiTiet;

    @DecimalMin(value = "0", message = "Thuế VAT không hợp lệ")
    @DecimalMax(value = "100", message = "Thuế VAT không hợp lệ")
    private BigDecimal tyLeVat;

    @DecimalMin(value = "0", message = "Phí giao hàng không hợp lệ")
    private BigDecimal phiGiaoHang;

    @DecimalMin(value = "0", message = "Tỷ lệ giảm giá không hợp lệ")
    @DecimalMax(value = "100", message = "Tỷ lệ giảm giá không hợp lệ")
    private BigDecimal tyLeGiamGiaTong;

    private LocalDate hanThanhToan;

    @Size(max = 2000, message = "Ghi chú không quá 2000 ký tự")
    private String ghiChu;
}