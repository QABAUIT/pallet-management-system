package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaoDonMuaRequest {

    @NotNull(message = "Chưa chọn nhà cung cấp")
    private Long nccId;

    private Long khoId; // bỏ trống = kho của người đang đăng nhập

    private LocalDate ngayDuKienNhan;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    @NotEmpty(message = "Đơn mua cần ít nhất 1 dòng hàng")
    @Valid
    private List<Dong> chiTiet;

    @Data
    public static class Dong {
        @NotNull(message = "Chưa chọn mặt hàng")
        private Long matHangId;

        @NotNull(message = "Chưa nhập số lượng")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;

        @NotNull(message = "Chưa nhập đơn giá")
        @PositiveOrZero(message = "Đơn giá phải >= 0")
        private BigDecimal donGia;
    }
}