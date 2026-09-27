package com.pallet.backend.entity;

import lombok.*;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính kép (loai_chung_tu, nam) cho bảng bo_dem_chung_tu.
 * Dùng để sinh số chứng từ tuần tự theo từng năm (VD: HD-2026-0001).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoDemChungTuId implements Serializable {

    private String loaiChungTu;

    private Integer nam;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoDemChungTuId)) return false;
        BoDemChungTuId that = (BoDemChungTuId) o;
        return Objects.equals(loaiChungTu, that.loaiChungTu) && Objects.equals(nam, that.nam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loaiChungTu, nam);
    }
}
