package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DanhMucDongPalletResponse {
    private List<DinhMucResponse> dinhMuc;      // các định mức đang hoạt động
    private List<VatTu> vatTu;                  // vật tư có thể dùng (thêm ngoài định mức)

    @Data
    @Builder
    public static class VatTu {
        private Long id;
        private String maMatHang;
        private String tenMatHang;
    }
}