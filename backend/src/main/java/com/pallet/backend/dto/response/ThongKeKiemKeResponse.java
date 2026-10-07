package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThongKeKiemKeResponse {
    private Integer tongPalletHeThong;
    private Integer daKiemTraThucTe;
    private Double tienDoPhanTram;
    private Integer phatHienChenhLech;
}