package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bang_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BangGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ngay_bat_dau_hieu_luc", nullable = false)
    private LocalDate ngayBatDauHieuLuc;

    @Column(name = "ngay_ket_thuc_hieu_luc")
    private LocalDate ngayKetThucHieuLuc;
}
