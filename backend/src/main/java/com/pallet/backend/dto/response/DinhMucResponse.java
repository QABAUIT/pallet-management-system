package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DinhMucResponse {
    private Long id;
    private String maDinhMuc;
    private String tenDinhMuc;
    private String trangThai;
    private Long thanhPhamId;
    private String maThanhPham;
    private String tenThanhPham;
    private String ghiChu;
    private List<Dong> chiTiet;

    @Data
    @Builder
    public static class Dong {
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private int soLuong;
    }
}