package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaoPhieuXuLyThemRequest {

    @NotNull(message = "Chưa chọn kho")
    private Long khoId;

    @NotBlank(message = "Chưa chọn loại nguồn (thanh hoặc cuc)")
    private String loaiNguon;

    @PositiveOrZero(message = "Số chiếc dùng lại phải >= 0")
    private Integer soDatLai;

    @PositiveOrZero(message = "Số chiếc bỏ phải >= 0")
    private Integer soPheLieu;

    @PositiveOrZero(message = "Số kg phế liệu phải >= 0")
    private Integer kgPheLieu;

    @NotBlank(message = "Bắt buộc nhập lý do xử lý")
    @Size(max = 255, message = "Lý do tối đa 255 ký tự")
    private String lyDo;
}