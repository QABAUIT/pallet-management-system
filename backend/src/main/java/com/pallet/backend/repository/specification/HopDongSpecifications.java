package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.HopDong;
import org.springframework.data.jpa.domain.Specification;

public class HopDongSpecifications {

    private HopDongSpecifications() {
    }

    public static Specification<HopDong> maHopDongChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("maHopDong")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<HopDong> loaiDoiTac(String loaiDoiTac) {
        if (loaiDoiTac == null || loaiDoiTac.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiDoiTac"), loaiDoiTac);
    }

    public static Specification<HopDong> khachHangId(Long khachHangId) {
        if (khachHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId);
    }

    public static Specification<HopDong> nccId(Long nccId) {
        if (nccId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ncc").get("id"), nccId);
    }

    public static Specification<HopDong> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }
}
