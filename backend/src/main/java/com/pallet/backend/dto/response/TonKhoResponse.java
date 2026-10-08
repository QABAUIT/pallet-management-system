package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TonKhoResponse {
    private Long matHangId;
    private String maSku;
    private String tenPallet;
    private String phanLoai;
    private String kichThuoc;
    private Integer soLuongTon;
    private BigDecimal donGiaDinhMuc;
    private String tinhTrang;
}