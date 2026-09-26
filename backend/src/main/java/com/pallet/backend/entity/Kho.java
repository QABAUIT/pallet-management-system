package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * loai_dia_diem: tru_so | kho | cang | khac
 * trang_thai: hoat_dong | ngung_hoat_dong
 */
@Entity
@Table(name = "kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_kho", nullable = false, unique = true, length = 20)
    private String maKho;

    @Column(name = "ten_kho", nullable = false, length = 150)
    private String tenKho;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "loai_dia_diem", nullable = false, length = 20)
    @Builder.Default
    private String loaiDiaDiem = "kho";

    @Column(name = "dien_tich_m2", precision = 12, scale = 2)
    private BigDecimal dienTichM2;

    // Vòng lặp FK với NhanVien (kho.quan_ly_id <-> nhan_vien.kho_id):
    // cẩn thận khi serialize JSON (dùng DTO hoặc @JsonIgnore ở tầng controller).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quan_ly_id")
    private NhanVien quanLy;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "hoat_dong";
}
