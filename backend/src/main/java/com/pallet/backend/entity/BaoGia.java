package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * trang_thai: nhap | da_gui | da_chap_nhan | tu_choi | het_han
 */
@Entity
@Table(name = "bao_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaoGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_bao_gia", nullable = false, unique = true, length = 20)
    private String maBaoGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id", nullable = false)
    private NhanVien nguoiTao;

    @Column(name = "ngay_tao", nullable = false)
    @Builder.Default
    private LocalDate ngayTao = LocalDate.now();

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "nhap";

    @Column(name = "ty_le_vat", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tyLeVat = new BigDecimal("8");

    @Column(name = "tong_tien", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(name = "tien_giam_gia_tong", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tienGiamGiaTong = BigDecimal.ZERO;

    @Column(name = "file_excel", length = 255)
    private String fileExcel;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
