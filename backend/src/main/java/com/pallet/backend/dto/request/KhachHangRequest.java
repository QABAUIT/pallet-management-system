package com.pallet.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 200, message = "Tên khách hàng không quá 200 ký tự")
    private String tenKh;

    @Pattern(regexp = "^(ca_nhan|doanh_nghiep)?$", message = "Loại khách hàng không hợp lệ")
    private String loaiKh; // để trống -> service tự gán mặc định "doanh_nghiep"

    @Size(max = 20, message = "MST không quá 20 ký tự")
    private String mst;

    @Size(max = 255, message = "Địa chỉ không quá 255 ký tự")
    private String diaChi;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ (10 chữ số)")
    @NotBlank(message = "Số điện thoại không được để trống")
    private String sdt;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email không quá 150 ký tự")
    private String email;

    @Size(max = 150, message = "Người đại diện không quá 150 ký tự")
    private String nguoiDaiDien;

    @Pattern(regexp = "^(dang_hop_tac|ngung_hop_tac)?$", message = "Trạng thái không hợp lệ")
    private String trangThai; // để trống -> service tự gán mặc định "dang_hop_tac"
}