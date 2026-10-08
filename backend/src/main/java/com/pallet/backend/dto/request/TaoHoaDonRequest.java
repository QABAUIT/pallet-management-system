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
import java.time.LocalDate;
import java.util.List;

@Data
public class TaoHoaDonRequest {

    @NotNull(message = "Vui lòng chọn khách hàng")
    private Long khachHangId;

    private Long khoId; // để trống = kho của nhân viên đang đăng nhập

    @NotEmpty(message = "Hóa đơn phải có ít nhất 1 mặt hàng")
    @Valid
    private List<DongHang> chiTiet;

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

    private boolean thanhToanNgay;
    private String phuongThucThanhToan; // tien_mat | chuyen_khoan | qr_code
    private BigDecimal tienKhachDua;

    @Data
    public static class DongHang {
        @NotNull(message = "Thiếu mã mặt hàng")
        private Long matHangId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;

        @DecimalMin(value = "0", message = "Chiết khấu không hợp lệ")
        @DecimalMax(value = "100", message = "Chiết khấu không hợp lệ")
        private BigDecimal tyLeChietKhau;
    }
}