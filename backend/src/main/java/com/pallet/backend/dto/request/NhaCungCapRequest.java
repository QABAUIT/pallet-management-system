package com.pallet.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhaCungCapRequest {

    @Size(max = 20, message = "Mã nhà cung cấp tối đa 20 ký tự")
    private String maNcc;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 200, message = "Tên nhà cung cấp tối đa 200 ký tự")
    private String tenNcc;

    @Size(max = 20, message = "Mã số thuế tối đa 20 ký tự")
    private String mst;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String sdt;

    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Size(max = 150, message = "Người liên hệ tối đa 150 ký tự")
    private String nguoiLienHe;

    @Size(max = 150, message = "Nhóm hàng tối đa 150 ký tự")
    private String nhomHang;

    private String ghiChu;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 20, message = "Trạng thái tối đa 20 ký tự")
    private String trangThai;
}