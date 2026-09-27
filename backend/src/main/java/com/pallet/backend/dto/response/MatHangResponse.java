package com.pallet.backend.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatHangResponse {
    private Long id;
    private String maMatHang;
    private String tenMatHang;
    private String loaiMatHang;
    private String chatLieu;
    private Integer kichThuocDai;
    private Integer kichThuocRong;
    private Integer kichThuocCao;
    private BigDecimal taiTrongTinh;
    private BigDecimal taiTrongDong;
    private String tieuChuan;
    private String nccMacDinhTen; // ten NCC, khong tra ca object cho gon payload
    private BigDecimal donGiaBan;
    private String hinhAnh;
    private String moTa;
    private String trangThai;

    // Tong hop nhanh tu bang ton_kho (SUM tat ca kho) - phuc vu man danh sach pallet
    private Integer tongTonKho;
    private Integer tongKhaDung; // = tongTonKho - tongDaGiuCho
}
