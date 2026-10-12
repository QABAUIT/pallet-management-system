package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThongKePhieuNhapResponse {
    private Integer nhapKhoTrongNgay; // Tổng pallet đã nhập hôm nay
    private Integer dangKiemDinh; // Tổng pallet đang chờ xử lý
    private Long tongGiaTriNhapThang; // Tính tiền tượng trưng

    private Double sucChuaKhaDungPhanTram;
    private Integer tongSoSlot;
    private Integer soSlotTrong;
}