package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.ChiPhiVanHanh;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ChiPhiVanHanhSpecifications {

    private ChiPhiVanHanhSpecifications() {
    }

    public static Specification<ChiPhiVanHanh> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<ChiPhiVanHanh> loaiChiPhi(String loaiChiPhi) {
        if (loaiChiPhi == null || loaiChiPhi.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiChiPhi"), loaiChiPhi);
    }

    public static Specification<ChiPhiVanHanh> ky(String ky) {
        if (ky == null || ky.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("ky"), ky);
    }

    public static Specification<ChiPhiVanHanh> ngayChiTu(LocalDate tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayChi"), tuNgay);
    }

    public static Specification<ChiPhiVanHanh> ngayChiDen(LocalDate denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayChi"), denNgay);
    }
}
