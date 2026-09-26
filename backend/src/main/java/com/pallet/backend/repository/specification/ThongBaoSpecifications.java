package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.ThongBao;
import org.springframework.data.jpa.domain.Specification;

public class ThongBaoSpecifications {

    private ThongBaoSpecifications() {
    }

    public static Specification<ThongBao> nguoiNhanId(Long nguoiNhanId) {
        if (nguoiNhanId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("nguoiNhan").get("id"), nguoiNhanId);
    }

    public static Specification<ThongBao> vaiTroNhanId(Long vaiTroNhanId) {
        if (vaiTroNhanId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("vaiTroNhan").get("id"), vaiTroNhanId);
    }

    public static Specification<ThongBao> daDoc(Boolean daDoc) {
        if (daDoc == null) return null;
        return (root, query, cb) -> cb.equal(root.get("daDoc"), daDoc);
    }

    public static Specification<ThongBao> loaiThongBao(String loaiThongBao) {
        if (loaiThongBao == null || loaiThongBao.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiThongBao"), loaiThongBao);
    }
}
