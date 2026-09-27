package com.pallet.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MatHangRequest {

    @NotBlank(message = "Tên mặt hàng không được để trống")
    private String tenMatHang;

    @NotBlank(message = "Loại mặt hàng bắt buộc: pallet hoặc linh_kien")
    private String loaiMatHang;

    private String chatLieu; // go | nhua | sat | khac

    private Integer kichThuocDai;
    private Integer kichThuocRong;
    private Integer kichThuocCao;
    private BigDecimal taiTrongTinh;
    private BigDecimal taiTrongDong;
    private String tieuChuan;

    private Long nccMacDinhId; // nullable

    @NotNull(message = "Đơn giá bán không được để trống")
    @PositiveOrZero(message = "Đơn giá bán phải >= 0")
    private BigDecimal donGiaBan;

    private String hinhAnh;
    private String moTa;

    // Chỉ dùng khi TẠO MỚI: số lượng tồn kho ban đầu, đúng theo màn hình "Thêm pallet mới"
    // trong Figma (form có ô "Số lượng" nhập luôn lúc tạo). Bỏ trống khi cập nhật.
    @Positive(message = "Số lượng ban đầu phải > 0")
    private Integer soLuongBanDau;

    // Kho nhận số lượng ban đầu - bắt buộc nếu soLuongBanDau khác null
    private Long khoId;
}
