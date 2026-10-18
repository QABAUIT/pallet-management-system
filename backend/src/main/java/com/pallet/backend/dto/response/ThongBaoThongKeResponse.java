package com.pallet.backend.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongBaoThongKeResponse {
    private long tongSo;
    private long chuaDoc;
    private long daDoc;
}
