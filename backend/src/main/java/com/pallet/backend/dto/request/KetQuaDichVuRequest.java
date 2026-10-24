package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class KetQuaDichVuRequest {

    @NotNull(message = "Chưa nhập số pallet đã tháo dỡ")
    @Positive(message = "Số pallet tháo dỡ phải lớn hơn 0")
    private Integer soPalletThaoDo;

    @PositiveOrZero(message = "Số thanh nguyên phải >= 0")
    private Integer soThanhNguyen;

    @PositiveOrZero(message = "Số cục vuông phải >= 0")
    private Integer soCucVuong;

    @PositiveOrZero(message = "Số thanh cần xử lý thêm phải >= 0")
    private Integer soThanhCanXuLy;

    @PositiveOrZero(message = "Số cục cần xử lý thêm phải >= 0")
    private Integer soCucCanXuLy;

    @PositiveOrZero(message = "Số kg phế liệu phải >= 0")
    private Integer kgPheLieu;
}