package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.MatHang;
import org.springframework.data.jpa.domain.Specification;

public class MatHangSpecifications {

    private MatHangSpecifications() {
    }

    public static Specification<MatHang> tenMatHangChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("tenMatHang")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<MatHang> maMatHang(String maMatHang) {
        if (maMatHang == null || maMatHang.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maMatHang"), maMatHang);
    }

    public static Specification<MatHang> loaiMatHang(String loaiMatHang) {
        if (loaiMatHang == null || loaiMatHang.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiMatHang"), loaiMatHang);
    }

    public static Specification<MatHang> chatLieu(String chatLieu) {
        if (chatLieu == null || chatLieu.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("chatLieu"), chatLieu);
    }

    public static Specification<MatHang> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }
}
