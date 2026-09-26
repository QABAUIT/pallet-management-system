package com.pallet.backend.repository.specification;

import com.pallet.backend.entity.Kho;
import org.springframework.data.jpa.domain.Specification;

/**
 * Các Specification lẻ dùng để ghép động, ví dụ trong Service:
 * <pre>
 *   Specification&lt;Kho&gt; spec = Specification
 *       .where(KhoSpecifications.tenKhoChua(tuKhoa))
 *       .and(KhoSpecifications.trangThai(trangThai));
 *   khoRepository.findAll(spec, pageable);
 * </pre>
 * Mọi method đều trả về null khi tham số rỗng để Specification.where(...).and(...)
 * tự bỏ qua điều kiện đó (Spring Data xử lý null-safe).
 */
public class KhoSpecifications {

    private KhoSpecifications() {
    }

    public static Specification<Kho> tenKhoChua(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("tenKho")), "%" + tuKhoa.toLowerCase() + "%");
    }

    public static Specification<Kho> maKho(String maKho) {
        if (maKho == null || maKho.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("maKho"), maKho);
    }

    public static Specification<Kho> trangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<Kho> loaiDiaDiem(String loaiDiaDiem) {
        if (loaiDiaDiem == null || loaiDiaDiem.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("loaiDiaDiem"), loaiDiaDiem);
    }
}
