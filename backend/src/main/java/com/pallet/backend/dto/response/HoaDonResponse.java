package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Một dòng trong danh sách hóa đơn / lịch sử giao dịch. */
@Data
@Builder
public class HoaDonResponse {
    private Long id;
    private String maHoaDon;
    private String loaiGiaoDich;
    private boolean laThu;
    private LocalDateTime ngayLap;
    private String trangThai;
    private boolean quaHan;
    private Long soNgayQuaHan;
    private LocalDate hanThanhToan;
    private BigDecimal tongThanhToan;
    private String phuongThucThanhToan;
    private boolean daChotSo;

    private String maDoiTac;
    private String tenDoiTac;
    private String mstDoiTac;
    private String sdtDoiTac;
    private String tenNhanVienLap;
}