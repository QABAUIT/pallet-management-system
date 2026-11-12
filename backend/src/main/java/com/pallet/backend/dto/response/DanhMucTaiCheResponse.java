package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DanhMucTaiCheResponse {
    private Long khoMacDinhId;
    private List<LoOption> loHang;

    @Data
    @Builder
    public static class LoOption {
        private Long id;
        private String maLo;
        private String tenMatHang;
        private Long khoId;
        private String tenKho;
        private String tenNhaCungCap;
        private LocalDate ngayNhap;
        private BigDecimal donGiaNhap;
        private int soLuongNhap;
        private int soLuongConLai;
        private int soLuongDangCho;   // đang nằm trong các phiếu nháp
        private int soLuongKhaDung;   // = còn lại - đang chờ
    }
}