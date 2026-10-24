package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DanhMucThaoDoResponse {
    private List<PhieuOption> phieuTaiChe;

    @Data
    @Builder
    public static class PhieuOption {
        private Long id;
        private String maPhieu;
        private String maLo;
        private Long khoId;
        private String tenKho;
        private int soCanThaoDo;      // tổng pallet phân loại vào nhóm tháo dỡ
        private int soDaDuyet;        // đã tháo dỡ xong (các đợt đã duyệt)
        private int soDangCho;        // nằm trong các đợt nháp
        private int soKhaDung;        // còn có thể tạo đợt mới
    }
}