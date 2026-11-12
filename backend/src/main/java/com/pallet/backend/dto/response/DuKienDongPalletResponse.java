package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DuKienDongPalletResponse {
    private Long dinhMucId;
    private Long khoId;
    private int soLuongSanXuat;
    private boolean duVatTu;
    private List<Dong> chiTiet;

    @Data
    @Builder
    public static class Dong {
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private int nhuCau;
        private int soLuongTon;      // tồn thực tế trong kho
        private int soLuongDaGiuCho; // đã giữ cho đơn hàng/việc khác
        private int soLuongDangCho;  // đang nằm trong các phiếu đóng pallet nháp khác
        private int khaDung;         // = tồn - đã giữ - đang chờ
        private int thieu;           // > 0 nghĩa là cần mua thêm hoặc tháo thêm
    }
}