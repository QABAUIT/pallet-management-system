package com.pallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HuyHoaDonRequest {
    @NotBlank(message = "Vui lòng nhập lý do hủy")
    @Size(max = 255, message = "Lý do không quá 255 ký tự")
    private String lyDo;
}