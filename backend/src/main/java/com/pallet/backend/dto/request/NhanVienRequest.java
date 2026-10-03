package com.pallet.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 150, message = "Họ và tên không quá 150 ký tự")
    private String hoTen;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email không quá 150 ký tự")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$|^$", message = "Số điện thoại không hợp lệ (10 chữ số)")
    private String sdt;

    private LocalDate ngaySinh;

    private String gioiTinh; // nam | nu | khac

    @Size(max = 255, message = "Địa chỉ không quá 255 ký tự")
    private String diaChi;

    @Size(max = 255, message = "Đường dẫn ảnh đại diện không quá 255 ký tự")
    private String anhDaiDien;

    @NotNull(message = "Vai trò không được để trống")
    private Long vaiTroId;

    private Long khoId; // Nullable nếu nhân viên không trực thuộc kho cụ thể

    private Long boPhanId; // Nullable

    @Size(max = 150, message = "Chức vụ không quá 150 ký tự")
    private String chucVu;

    @Size(max = 100, message = "Loại hợp đồng không quá 100 ký tự")
    private String loaiHopDong;

    @Size(max = 50, message = "Số hợp đồng không quá 50 ký tự")
    private String soHopDong;

    // Bắt buộc khi tạo mới. Khi cập nhật có thể giữ nguyên tên đăng nhập cũ.
    @Size(min = 4, max = 50, message = "Tên đăng nhập từ 4 đến 50 ký tự")
    private String tenDangNhap;

    // Mật khẩu dạng raw text từ client. Bắt buộc khi tạo mới, để trống khi chỉ cập
    // nhật thông tin cá nhân.
    private String matKhau;

    private LocalDate ngayVaoLam;

    private LocalDate ngayNghiViec;

    private String trangThai; // dang_lam_viec | da_nghi_viec
}