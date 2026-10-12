package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** Dữ liệu cho form "Tạo hóa đơn": danh sách khách hàng + mặt hàng kèm tồn khả dụng của kho. */
@Data
@Builder
public class DanhMucBanHangResponse {
    private Long khoId;
    private String tenKho;
    private List<KhachHangOption> khachHang;
    private List<MatHangOption> matHang;

    @Data
    @Builder
    public static class KhachHangOption {
        private Long id;
        private String maKh;
        private String tenKh;
        private String sdt;
        private String mst;
    }

    @Data
    @Builder
    public static class MatHangOption {
        private Long id;
        private String maMatHang;
        private String tenMatHang;
        private String moTa;
        private String kichThuoc;
        private BigDecimal donGia;
        private Integer tonKhaDung;
    }
}