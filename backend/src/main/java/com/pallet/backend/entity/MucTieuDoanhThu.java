package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * ky: thang | quy | nam
 * Khi ky = 'thang' thì thangKy bắt buộc 1-12 (DB CHECK), ngược lại phải NULL.
 */
@Entity
@Table(name = "muc_tieu_doanh_thu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MucTieuDoanhThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id")
    private Kho kho;

    @Column(name = "ky", nullable = false, length = 10)
    private String ky;

    @Column(name = "nam_ky", nullable = false)
    private Integer namKy;

    @Column(name = "thang_ky")
    private Short thangKy;

    @Column(name = "so_tien_muc_tieu", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienMucTieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id")
    private NhanVien nguoiTao;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
