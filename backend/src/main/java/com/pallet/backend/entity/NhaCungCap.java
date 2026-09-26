package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * trang_thai: dang_hop_tac | ngung_hop_tac
 */
@Entity
@Table(name = "nha_cung_cap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhaCungCap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_ncc", nullable = false, unique = true, length = 20)
    private String maNcc;

    @Column(name = "ten_ncc", nullable = false, length = 200)
    private String tenNcc;

    @Column(name = "mst", length = 20)
    private String mst;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "nguoi_lien_he", length = 150)
    private String nguoiLienHe;

    @Column(name = "nhom_hang", length = 150)
    private String nhomHang;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "dang_hop_tac";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private NhanVien createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
