package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.NhatKyHeThong;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class NhatKyHeThongSpecifications {

    private NhatKyHeThongSpecifications() {
    }

    public static Specification<NhatKyHeThong> nhanVienId(Long nhanVienId) {
        if (nhanVienId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("nhanVien").get("id"), nhanVienId);
    }

    public static Specification<NhatKyHeThong> module(String module) {
        if (module == null || module.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("module"), module);
    }

    public static Specification<NhatKyHeThong> thoiGianTu(LocalDateTime tuThoiGian) {
        if (tuThoiGian == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("thoiGian"), tuThoiGian);
    }

    public static Specification<NhatKyHeThong> thoiGianDen(LocalDateTime denThoiGian) {
        if (denThoiGian == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("thoiGian"), denThoiGian);
    }
}
