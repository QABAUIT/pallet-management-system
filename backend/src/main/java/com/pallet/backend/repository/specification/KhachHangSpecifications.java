package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.KhachHang;
import org.springframework.data.jpa.domain.Specification;

public class KhachHangSpecifications {

    private KhachHangSpecifications() {
    }

    public static Specification<KhachHang> tenKhChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("tenKh")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<KhachHang> maKh(String maKh) {
        if (maKh == null || maKh.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maKh"), maKh);
    }

    public static Specification<KhachHang> loaiKh(String loaiKh) {
        if (loaiKh == null || loaiKh.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiKh"), loaiKh);
    }

    public static Specification<KhachHang> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }
}
