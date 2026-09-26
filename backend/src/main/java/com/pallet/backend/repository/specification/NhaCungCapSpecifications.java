package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.NhaCungCap;
import org.springframework.data.jpa.domain.Specification;

public class NhaCungCapSpecifications {

    private NhaCungCapSpecifications() {
    }

    public static Specification<NhaCungCap> tenNccChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("tenNcc")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<NhaCungCap> maNcc(String maNcc) {
        if (maNcc == null || maNcc.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maNcc"), maNcc);
    }

    public static Specification<NhaCungCap> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }
}
