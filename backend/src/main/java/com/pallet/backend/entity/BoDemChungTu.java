package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Bộ đếm chứng từ theo (loai_chung_tu, nam) - dùng để sinh mã chứng từ tự động.
 * KHUYẾN NGHỊ: không tăng so_hien_tai trực tiếp qua entity này bằng save()
 * thông thường (dễ race-condition khi nhiều giao dịch chạy song song).
 * Hãy gọi hàm DB fn_lay_ma_chung_tu(p_loai_chung_tu, p_tien_to) qua
 * @Query(nativeQuery = true) hoặc EntityManager.createNativeQuery, vì hàm đó
 * dùng INSERT ... ON CONFLICT DO UPDATE ... RETURNING nên an toàn tuyệt đối
 * với đồng thời (concurrency-safe) ở cấp DB.
 */
@Entity
@Table(name = "bo_dem_chung_tu")
@IdClass(BoDemChungTuId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoDemChungTu {

    @Id
    @Column(name = "loai_chung_tu", length = 20)
    private String loaiChungTu;

    @Id
    @Column(name = "nam")
    private Integer nam;

    @Column(name = "so_hien_tai", nullable = false)
    @Builder.Default
    private Integer soHienTai = 0;
}
