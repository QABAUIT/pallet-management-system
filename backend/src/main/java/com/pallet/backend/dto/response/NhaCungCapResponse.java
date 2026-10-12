package com.pallet.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhaCungCapResponse {
    private Long id;
    private String maNcc;
    private String tenNcc;
    private String mst;
    private String diaChi;
    private String sdt;
    private String email;
    private String nguoiLienHe;
    private String nhomHang;
    private String ghiChu;
    private String trangThai;
    private LocalDateTime createdAt;
    private String createdBy; 
}