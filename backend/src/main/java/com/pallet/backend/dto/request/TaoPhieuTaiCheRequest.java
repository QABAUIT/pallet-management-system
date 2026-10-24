package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaoPhieuTaiCheRequest {

    @NotNull(message = "Chưa chọn lô hàng")
    private Long loHangId;

    @NotNull(message = "Chưa nhập số pallet xử lý")
    @Positive(message = "Số pallet xử lý phải lớn hơn 0")
    private Integer soLuongXuLy;

    @NotNull(message = "Chưa nhập số pallet bán lại")
    @PositiveOrZero(message = "Số pallet bán lại phải >= 0")
    private Integer soBanLai;

    @NotNull(message = "Chưa nhập số pallet tháo dỡ")
    @PositiveOrZero(message = "Số pallet tháo dỡ phải >= 0")
    private Integer soThaoDo;

    @NotNull(message = "Chưa nhập số pallet loại bỏ")
    @PositiveOrZero(message = "Số pallet loại bỏ phải >= 0")
    private Integer soLoaiBo;

    @PositiveOrZero(message = "Số kg phế liệu phải >= 0")
    private Integer kgPheLieuLoaiBo; // bỏ trống = 0

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;
}