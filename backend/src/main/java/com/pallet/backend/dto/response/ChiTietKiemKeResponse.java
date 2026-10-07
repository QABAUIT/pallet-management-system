package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChiTietKiemKeResponse {
    private Long id;
    private String maSku;
    private String tenPallet;
    private String viTriLuuTru;
    private Integer tonHeThong;
    private Integer tonThucTe;
    private Integer chenhLech;
    private String trangThaiKiemKe; // "khop", "lech", "cho_kiem"
}