package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ThongKeTonKhoResponse {
    private Integer tongSoLuongTon;
    private BigDecimal tongGiaTriUocTinh;
    private Integer soChungLoaiCanhBao;
}