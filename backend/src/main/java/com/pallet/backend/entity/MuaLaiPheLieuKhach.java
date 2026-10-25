package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mua_lai_phe_lieu_khach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuaLaiPheLieuKhach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dich_vu_id", nullable = false)
    private PhieuDichVuThaoDo phieuDichVu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_kho_id", nullable = false)
    private PhieuKho phieuKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "so_kg", nullable = false)
    private int soKg;

    @Column(name = "don_gia_kg", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGiaKg;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void truocKhiLuu() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}