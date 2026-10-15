package com.pallet.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class XepViTriRequest {
    private List<Item> items;

    @Data
    public static class Item {
        private Long matHangId;
        private Long khoId;    // Mới: Kho đích đến
        private Long viTriId;  // Vị trí đích đến thuộc kho trên
    }
}