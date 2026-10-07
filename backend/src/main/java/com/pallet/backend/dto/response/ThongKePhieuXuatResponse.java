package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThongKePhieuXuatResponse {
    private Integer tongXuatHomNay; 
    private Integer dangBocDoXepXe; 
    private Integer xuatBaoDuongDinhKy; 
    private Double tyLeXuatDungTienDo; 
}