package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.BaoGia;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BaoGiaSpecifications {

    private BaoGiaSpecifications() {
    }

    public static Specification<BaoGia> maBaoGiaChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("maBaoGia")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<BaoGia> khachHangId(Long khachHangId) {
        if (khachHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId);
    }

    public static Specification<BaoGia> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<BaoGia> ngayTaoTu(LocalDate tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayTao"), tuNgay);
    }

    public static Specification<BaoGia> ngayTaoDen(LocalDate denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayTao"), denNgay);
    }
}
