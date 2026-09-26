package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sua_chua_pallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuaChuaPallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "mo_ta_cong_viec", length = 255)
    private String moTaCongViec;

    @Column(name = "chi_phi", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal chiPhi = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_thuc_hien_id")
    private NhanVien nhanVienThucHien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id")
    private HoaDon hoaDon;

    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;
}
