package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * loai_kh: ca_nhan | doanh_nghiep
 * trang_thai: dang_hop_tac | ngung_hop_tac
 */
@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_kh", nullable = false, unique = true, length = 20)
    private String maKh;

    @Column(name = "ten_kh", nullable = false, length = 200)
    private String tenKh;

    @Column(name = "loai_kh", length = 20)
    @Builder.Default
    private String loaiKh = "doanh_nghiep";

    @Column(name = "mst", length = 20)
    private String mst;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "nguoi_dai_dien", length = 150)
    private String nguoiDaiDien;

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
