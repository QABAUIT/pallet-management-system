package com.pallet.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChiPhiVanHanhRequest {

    private Long khoId; // để trống = kho của người đang đăng nhập

    @NotBlank(message = "Vui lòng chọn loại chi phí")
    @Pattern(regexp = "^(van_tai_logistics|nhan_su|bao_tri_thiet_bi|dien_nang_luong|khac)$",
            message = "Loại chi phí không hợp lệ")
    private String loaiChiPhi;

    @Size(max = 255, message = "Mô tả không quá 255 ký tự")
    private String moTa;

    @NotNull(message = "Vui lòng nhập số tiền")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    private BigDecimal soTien;

    @Pattern(regexp = "^(thang|quy|nam)?$", message = "Kỳ không hợp lệ")
    private String ky; // để trống = thang

    @NotNull(message = "Vui lòng chọn ngày chi")
    private LocalDate ngayChi;

    @Size(max = 255, message = "Ghi chú không quá 255 ký tự")
    private String ghiChu;
}