package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TonCanXuLyResponse {
    private List<Dong> dong;

    @Data
    @Builder
    public static class Dong {
        private Long khoId;
        private String tenKho;
        private String loaiNguon;     // thanh | cuc
        private String tenMatHang;
        private int soLuongTon;
    }
}