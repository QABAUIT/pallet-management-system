package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.NhanVien;
import org.springframework.data.jpa.domain.Specification;

public class NhanVienSpecifications {

    private NhanVienSpecifications() {
    }

    public static Specification<NhanVien> hoTenChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("hoTen")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<NhanVien> maNv(String maNv) {
        if (maNv == null || maNv.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maNv"), maNv);
    }

    public static Specification<NhanVien> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<NhanVien> vaiTroId(Long vaiTroId) {
        if (vaiTroId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("vaiTro").get("id"), vaiTroId);
    }

    public static Specification<NhanVien> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<NhanVien> boPhanId(Long boPhanId) {
        if (boPhanId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("boPhan").get("id"), boPhanId);
    }
}
