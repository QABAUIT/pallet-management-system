package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "chi_phi_van_hanh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiPhiVanHanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id")
    private NhanVien nguoiTao;

    @Column(name = "ma_chi_phi", nullable = false, unique = true, length = 20)
    private String maChiPhi;

    @Column(name = "loai_chi_phi", nullable = false, length = 30)
    private String loaiChiPhi;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "so_tien", nullable = false)
    private BigDecimal soTien;

    @Column(name = "ky", nullable = false, length = 10)
    private String ky;

    @Column(name = "ngay_chi", nullable = false)
    private LocalDate ngayChi;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

}