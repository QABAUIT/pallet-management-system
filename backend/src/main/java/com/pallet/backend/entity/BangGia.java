package com.pallet.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "don_gia", nullable = false)
    private BigDecimal donGia;

    @Column(name = "ngay_bat_dau_hieu_luc", nullable = false)
    private LocalDate ngayBatDauHieuLuc;

    @Column(name = "ngay_ket_thuc_hieu_luc")
    private LocalDate ngayKetThucHieuLuc;

}