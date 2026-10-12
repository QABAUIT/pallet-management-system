package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PhieuNhapResponse {
    private Long id;
    private String maPhieu;
    private String tenNhaCungCap;
    private String tenNhanVienTiepNhan;
    private LocalDateTime ngayNhapKho;
    private Integer tongSoLuongPallet;
    private String trangThai;
}