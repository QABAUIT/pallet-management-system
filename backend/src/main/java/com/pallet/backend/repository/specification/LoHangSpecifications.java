package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.LoHang;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class LoHangSpecifications {

    private LoHangSpecifications() {
    }

    public static Specification<LoHang> maLo(String maLo) {
        if (maLo == null || maLo.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maLo"), maLo);
    }

    public static Specification<LoHang> matHangId(Long matHangId) {
        if (matHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("matHang").get("id"), matHangId);
    }

    public static Specification<LoHang> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<LoHang> nccId(Long nccId) {
        if (nccId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ncc").get("id"), nccId);
    }

    public static Specification<LoHang> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<LoHang> ngayNhapTu(LocalDate tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayNhap"), tuNgay);
    }

    public static Specification<LoHang> ngayNhapDen(LocalDate denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayNhap"), denNgay);
    }
}
