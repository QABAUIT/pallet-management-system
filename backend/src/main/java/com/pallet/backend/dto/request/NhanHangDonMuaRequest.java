package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NhanHangDonMuaRequest {

    @Size(max = 20, message = "Biển số xe tối đa 20 ký tự")
    private String bienSoXe;

    private String ghiChu;

    @NotEmpty(message = "Chưa chọn dòng hàng nào để nhận")
    @Valid
    private List<Dong> chiTiet;

    @Data
    public static class Dong {
        @NotNull(message = "Thiếu mã dòng hàng của đơn mua")
        private Long chiTietId;

        @NotNull(message = "Chưa nhập số lượng nhận")
        @Positive(message = "Số lượng nhận phải lớn hơn 0")
        private Integer soLuongNhan;

        private String ghiChu;
    }
}