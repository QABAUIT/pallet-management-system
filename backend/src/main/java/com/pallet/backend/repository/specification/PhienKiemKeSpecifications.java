package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.PhienKiemKe;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PhienKiemKeSpecifications {

    private PhienKiemKeSpecifications() {
    }

    public static Specification<PhienKiemKe> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<PhienKiemKe> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<PhienKiemKe> ngayBatDauTu(LocalDate tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayBatDau"), tuNgay);
    }

    public static Specification<PhienKiemKe> ngayBatDauDen(LocalDate denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayBatDau"), denNgay);
    }
}
