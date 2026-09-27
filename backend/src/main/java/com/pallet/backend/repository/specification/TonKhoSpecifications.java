package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.TonKho;
import org.springframework.data.jpa.domain.Specification;

public class TonKhoSpecifications {

    private TonKhoSpecifications() {
    }

    public static Specification<TonKho> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<TonKho> matHangId(Long matHangId) {
        if (matHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("matHang").get("id"), matHangId);
    }

    /** Chỉ lấy các dòng tồn khả dụng (tồn - giữ chỗ) < mức tối thiểu. */
    public static Specification<TonKho> duoiMucToiThieu() {
        return (root, query, cb) -> cb.lessThan(
                cb.diff(root.<Integer>get("soLuongTonKho"), root.<Integer>get("soLuongDaGiuCho")),
                root.<Integer>get("soLuongToiThieu")
        );
    }
}