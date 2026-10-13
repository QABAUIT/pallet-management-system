package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BaoCaoThueResponse {
    private BigDecimal tongDoanhThuChuaThue;
    private BigDecimal tongThue;
    private BigDecimal tyLeVatMacDinh; // %, lấy từ bảng company
    private List<Dong> chiTiet;

    @Data
    @Builder
    public static class Dong {
        private Long id;
        private String maHoaDon;
        private LocalDateTime ngayLap;
        private String tenKhachHang;
        private BigDecimal doanhThuChuaThue;
        private BigDecimal tyLeVat;
        private BigDecimal tienVat;
        private String trangThai;
    }
}