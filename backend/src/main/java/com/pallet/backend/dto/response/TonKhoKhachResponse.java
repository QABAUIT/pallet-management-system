package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TonKhoKhachResponse {
    private List<Dong> dong;

    @Data
    @Builder
    public static class Dong {
        private Long khachHangId;
        private String tenKhachHang;
        private Long khoId;
        private String tenKho;
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private String donViTinh;
        private int soLuong;
    }
}