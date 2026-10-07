package com.pallet.backend.dto.request;

import lombok.Data;

@Data
public class TaoPhienKiemKeRequest {
    private Long khoId;
    private Long nguoiTaoId;
    private String ghiChu;
}