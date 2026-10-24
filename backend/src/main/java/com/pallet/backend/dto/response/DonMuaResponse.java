package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DonMuaResponse {
    private Long id;
    private String maDonMua;
    private String tenNhaCungCap;
    private String tenKho;
    private String tenNhanVienLap;
    private LocalDate ngayDat;
    private LocalDate ngayDuKienNhan;
    private String trangThai;
    private boolean treHan;          // quá ngày dự kiến mà chưa nhận đủ
    private BigDecimal tongTien;
    private int tongSoLuongDat;
    private int tongSoLuongDaNhan;
}