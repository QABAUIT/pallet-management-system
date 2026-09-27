package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Bộ đếm sinh mã chứng từ tuần tự theo (loại chứng từ, năm).
 * VD: loai_chung_tu = "HD", nam = 2026, so_hien_tai = 1 -> mã tiếp theo là HD-2026-0002.
 *
 * QUAN TRỌNG: khi tăng so_hien_tai trong code PHẢI dùng khóa bi quan
 * (xem BoDemChungTuRepository.findForUpdate) và nằm trong 1 @Transactional,
 * tránh 2 người tạo hóa đơn/phiếu cùng lúc bị sinh trùng số chứng từ.
 */
@Entity
@Table(name = "bo_dem_chung_tu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoDemChungTu {

    @EmbeddedId
    private BoDemChungTuId id;

    @Column(name = "so_hien_tai", nullable = false)
    private Integer soHienTai;
}
