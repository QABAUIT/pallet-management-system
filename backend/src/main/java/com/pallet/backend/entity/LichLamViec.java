package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lich_lam_viec",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nhan_vien_id", "ngay"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ca_lam_viec_id", nullable = false)
    private CaLamViec caLamViec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id")
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xep_lich_id")
    private NhanVien nguoiXepLich;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
