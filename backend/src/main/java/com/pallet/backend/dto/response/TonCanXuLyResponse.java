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
        private int soLuongTon;       // tồn thực tế trong kho
        private int soLuongDaGiuCho;  // đã giữ cho đơn hàng/việc khác
        private int soLuongDangCho;   // đang nằm trong các phiếu nháp khác
        private int soLuongKhaDung;   // = tồn - đã giữ - đang chờ
    }
}