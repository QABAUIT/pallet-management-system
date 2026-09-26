package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.BangGia;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BangGiaSpecifications {

    private BangGiaSpecifications() {
    }

    public static Specification<BangGia> matHangId(Long matHangId) {
        if (matHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("matHang").get("id"), matHangId);
    }

    public static Specification<BangGia> khachHangId(Long khachHangId) {
        if (khachHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId);
    }

    /** Đang hiệu lực tại 1 thời điểm cho trước. */
    public static Specification<BangGia> hieuLucTaiNgay(LocalDate ngay) {
        if (ngay == null) return null;
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("ngayBatDauHieuLuc"), ngay),
                cb.or(
                        cb.isNull(root.get("ngayKetThucHieuLuc")),
                        cb.greaterThanOrEqualTo(root.get("ngayKetThucHieuLuc"), ngay)
                )
        );
    }
}
