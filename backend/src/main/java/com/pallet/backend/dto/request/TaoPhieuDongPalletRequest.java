package com.pallet.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TaoPhieuDongPalletRequest {

    @NotNull(message = "Chưa chọn định mức")
    private Long dinhMucId;

    @NotNull(message = "Chưa chọn kho")
    private Long khoId;

    @NotNull(message = "Chưa nhập số pallet sản xuất")
    @Positive(message = "Số pallet sản xuất phải lớn hơn 0")
    private Integer soLuongSanXuat;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    /** Tùy chọn: ghi đè số thực tế của từng vật tư (hoặc thêm vật tư ngoài định mức). Bỏ trống = đúng định mức. */
    @Valid
    private List<Dong> chiTiet;

    @Data
    public static class Dong {
        @NotNull(message = "Chưa chọn vật tư")
        private Long matHangId;

        @NotNull(message = "Chưa nhập số lượng thực tế")
        @PositiveOrZero(message = "Số lượng thực tế phải >= 0")
        private Integer soLuongThucTe;

        @Size(max = 255, message = "Lý do tối đa 255 ký tự")
        private String lyDoChenhLech;
    }
}