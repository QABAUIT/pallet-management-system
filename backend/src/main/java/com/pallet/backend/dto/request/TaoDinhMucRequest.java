package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TaoDinhMucRequest {

    @NotBlank(message = "Chưa nhập tên định mức")
    @Size(max = 150, message = "Tên định mức tối đa 150 ký tự")
    private String tenDinhMuc;

    @NotNull(message = "Chưa chọn thành phẩm")
    private Long thanhPhamId;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    @NotEmpty(message = "Định mức phải có ít nhất một vật tư")
    @Valid
    private List<Dong> chiTiet;

    @Data
    public static class Dong {
        @NotNull(message = "Chưa chọn vật tư")
        private Long matHangId;

        @NotNull(message = "Chưa nhập số lượng")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;
    }
}