package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.PhieuKho;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PhieuKhoSpecifications {

    private PhieuKhoSpecifications() {
    }

    public static Specification<PhieuKho> maPhieuChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("maPhieu")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<PhieuKho> loaiPhieu(String loaiPhieu) {
        if (loaiPhieu == null || loaiPhieu.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiPhieu"), loaiPhieu);
    }

    public static Specification<PhieuKho> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<PhieuKho> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<PhieuKho> khoDoiUngId(Long khoDoiUngId) {
        if (khoDoiUngId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("khoDoiUng").get("id"), khoDoiUngId);
    }

    public static Specification<PhieuKho> ngayGioTu(LocalDateTime tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayGio"), tuNgay);
    }

    public static Specification<PhieuKho> ngayGioDen(LocalDateTime denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayGio"), denNgay);
    }
}
