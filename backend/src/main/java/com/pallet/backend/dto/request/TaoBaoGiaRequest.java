package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TaoBaoGiaRequest {

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Long khachHangId;

    @DecimalMin(value = "0", message = "Thuế VAT không hợp lệ")
    @DecimalMax(value = "100", message = "Thuế VAT không hợp lệ")
    private BigDecimal tyLeVat; // bỏ trống = 8%

    @DecimalMin(value = "0", message = "Tỷ lệ giảm giá không hợp lệ")
    @DecimalMax(value = "100", message = "Tỷ lệ giảm giá không hợp lệ")
    private BigDecimal tyLeGiamGiaTong;

    private Integer soNgayHieuLuc; // bỏ trống = 15 ngày

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    @NotEmpty(message = "Báo giá phải có ít nhất 1 mặt hàng")
    @Valid
    private List<Dong> chiTiet;

    @Data
    public static class Dong {
        @NotNull(message = "Thiếu mã mặt hàng")
        private Long matHangId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;

        @DecimalMin(value = "0", message = "Đơn giá không hợp lệ")
        private BigDecimal donGia; // bỏ trống = giá bán hiện hành của mặt hàng

        @DecimalMin(value = "0", message = "Chiết khấu không hợp lệ")
        @DecimalMax(value = "100", message = "Chiết khấu không hợp lệ")
        private BigDecimal tyLeChietKhau;
    }
}