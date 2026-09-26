package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.PheDuyet;
import org.springframework.data.jpa.domain.Specification;

public class PheDuyetSpecifications {

    private PheDuyetSpecifications() {
    }

    public static Specification<PheDuyet> loaiYeuCau(String loaiYeuCau) {
        if (loaiYeuCau == null || loaiYeuCau.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiYeuCau"), loaiYeuCau);
    }

    public static Specification<PheDuyet> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<PheDuyet> nguoiYeuCauId(Long nguoiYeuCauId) {
        if (nguoiYeuCauId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("nguoiYeuCau").get("id"), nguoiYeuCauId);
    }

    public static Specification<PheDuyet> doiTuong(String doiTuongLoai, Long doiTuongId) {
        if (doiTuongLoai == null || doiTuongId == null) return null;
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("doiTuongLoai"), doiTuongLoai),
                cb.equal(root.get("doiTuongId"), doiTuongId)
        );
    }
}
