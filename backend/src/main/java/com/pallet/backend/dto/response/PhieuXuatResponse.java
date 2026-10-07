package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PhieuXuatResponse {
    private Long id;
    private String maPhieuXuat;
    private String maNhanVienXuat;
    private String tenNhanVienXuat;
    private LocalDateTime ngayXuatKho;
    private String loaiXuatKho;
    private String lyDoXuat;
    private Integer tongSoLuong;
    private String trangThai;
}