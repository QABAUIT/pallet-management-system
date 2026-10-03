package com.pallet.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaoTaiKhoanRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu từ 6 đến 72 ký tự")
    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150)
    private String hoTen;

    @NotBlank(message = "Vai trò không được để trống")
    private String maVaiTro;          // GD, PGD, NVKHO, NVBANHANG

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 20)
    private String sdt;

    private LocalDate ngayVaoLam;
}