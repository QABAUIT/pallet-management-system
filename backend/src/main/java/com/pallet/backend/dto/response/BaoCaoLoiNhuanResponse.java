package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BaoCaoLoiNhuanResponse {
    private BigDecimal doanhThu;
    private BigDecimal chiPhi;
    private BigDecimal loiNhuan;
    private Double phanTramDoanhThu;
    private Double phanTramChiPhi;
    private Double phanTramLoiNhuan;
    private List<DiemSoSanh> bieuDo;
    private List<HangMuc> chiPhiTheoHangMuc;

    @Data
    @Builder
    public static class DiemSoSanh {
        private String nhan;
        private BigDecimal doanhThu;
        private BigDecimal chiPhi;
    }

    @Data
    @Builder
    public static class HangMuc {
        private String ma;
        private String ten;
        private BigDecimal soTien;
    }
}