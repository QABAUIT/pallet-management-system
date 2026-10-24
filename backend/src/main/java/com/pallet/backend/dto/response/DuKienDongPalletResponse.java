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
        private int khaDung;
        private int thieu;   // > 0 nghĩa là cần mua thêm hoặc tháo thêm
    }
}