package com.pallet.backend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Khoá chính phức hợp (composite key) của bo_dem_chung_tu (loai_chung_tu, nam).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoDemChungTuId implements Serializable {

    private String loaiChungTu;
    private Integer nam;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoDemChungTuId that)) return false;
        return Objects.equals(loaiChungTu, that.loaiChungTu) && Objects.equals(nam, that.nam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loaiChungTu, nam);
    }
}
