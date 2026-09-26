package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * trang_thai: con_hang | da_het
 */
@Entity
@Table(name = "lo_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_lo", nullable = false, unique = true, length = 30)
    private String maLo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @Column(name = "so_luong_nhap", nullable = false)
    private Integer soLuongNhap;

    @Column(name = "so_luong_con_lai", nullable = false)
    private Integer soLuongConLai;

    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate ngayNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id")
    private NhaCungCap ncc;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "con_hang";
}
