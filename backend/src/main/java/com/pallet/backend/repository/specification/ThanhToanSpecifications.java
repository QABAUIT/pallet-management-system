package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.ThanhToan;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ThanhToanSpecifications {

    private ThanhToanSpecifications() {
    }

    public static Specification<ThanhToan> hoaDonId(Long hoaDonId) {
        if (hoaDonId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("hoaDon").get("id"), hoaDonId);
    }

    public static Specification<ThanhToan> phuongThuc(String phuongThuc) {
        if (phuongThuc == null || phuongThuc.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("phuongThuc"), phuongThuc);
    }

    public static Specification<ThanhToan> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<ThanhToan> thoiGianTu(LocalDateTime tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("thoiGian"), tuNgay);
    }

    public static Specification<ThanhToan> thoiGianDen(LocalDateTime denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("thoiGian"), denNgay);
    }
}
