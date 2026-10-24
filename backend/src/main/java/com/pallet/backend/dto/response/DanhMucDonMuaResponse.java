package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DanhMucDonMuaResponse {
    private Long khoMacDinhId;
    private List<KhoOption> kho;
    private List<NccOption> nhaCungCap;
    private List<MatHangOption> matHang;

    @Data
    @Builder
    public static class KhoOption {
        private Long id;
        private String maKho;
        private String tenKho;
    }

    @Data
    @Builder
    public static class NccOption {
        private Long id;
        private String maNcc;
        private String tenNcc;
        private String nhomHang;
    }

    @Data
    @Builder
    public static class MatHangOption {
        private Long id;
        private String maMatHang;
        private String tenMatHang;
        private String loaiMatHang;
        private String chatLieu;
        private String donViTinh;
    }
}