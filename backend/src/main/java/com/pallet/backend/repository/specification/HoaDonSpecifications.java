package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.HoaDon;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class HoaDonSpecifications {

    private HoaDonSpecifications() {
    }

    public static Specification<HoaDon> maHoaDonChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("maHoaDon")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<HoaDon> loaiGiaoDich(String loaiGiaoDich) {
        if (loaiGiaoDich == null || loaiGiaoDich.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiGiaoDich"), loaiGiaoDich);
    }

    public static Specification<HoaDon> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<HoaDon> khachHangId(Long khachHangId) {
        if (khachHangId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId);
    }

    public static Specification<HoaDon> nccId(Long nccId) {
        if (nccId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ncc").get("id"), nccId);
    }

    public static Specification<HoaDon> khoId(Long khoId) {
        if (khoId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("kho").get("id"), khoId);
    }

    public static Specification<HoaDon> daChotSo(Boolean daChotSo) {
        if (daChotSo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("daChotSo"), daChotSo);
    }

    public static Specification<HoaDon> ngayLapTu(LocalDateTime tuNgay) {
        if (tuNgay == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ngayLap"), tuNgay);
    }

    public static Specification<HoaDon> ngayLapDen(LocalDateTime denNgay) {
        if (denNgay == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ngayLap"), denNgay);
    }
}
