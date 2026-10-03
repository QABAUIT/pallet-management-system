package com.pallet.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienResponse {

    private Long id;
    private String maNv;
    private String hoTen;
    private String email;
    private String sdt;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String anhDaiDien;

    // Thông tin chức vụ và tổ chức
    private Long vaiTroId;
    private String tenVaiTro;

    private Long khoId;
    private String tenKho;

    private Long boPhanId;
    private String tenBoPhan;

    private String chucVu;
    private String loaiHopDong;
    private String soHopDong;

    // Thông tin tài khoản & làm việc
    private String tenDangNhap;
    private LocalDate ngayVaoLam;
    private LocalDate ngayNghiViec;
    private String trangThai;

    // Audit timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}