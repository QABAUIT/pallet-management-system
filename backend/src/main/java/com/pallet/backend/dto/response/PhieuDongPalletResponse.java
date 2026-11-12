package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PhieuDongPalletResponse {
    private Long id;
    private String maPhieu;
    private String trangThai;
    private Long dinhMucId;
    private String tenDinhMuc;
    private Long thanhPhamId;
    private String tenThanhPham;
    private Long khoId;
    private String tenKho;
    private int soLuongSanXuat;
    private String tenNguoiThucHien;
    private String tenNguoiDuyet;
    private LocalDate ngayThucHien;
    private LocalDateTime thoiGianDuyet;
    private LocalDateTime createdAt;
    private String ghiChu;
    private String lyDoHuy;
    private int soDongChenhLech;      // số dòng vật tư lệch định mức
    private List<Dong> chiTiet;       // chỉ có ở màn chi tiết

    @Data
    @Builder
    public static class Dong {
        private Long matHangId;
        private String maMatHang;
        private String tenMatHang;
        private int soLuongDinhMuc;
        private int soLuongThucTe;
        private int chenhLech;
        private String lyDoChenhLech;
    }
}