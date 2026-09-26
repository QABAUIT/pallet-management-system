package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phien_dang_nhap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhienDangNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    @Column(name = "ghi_nho_dang_nhap", nullable = false)
    @Builder.Default
    private Boolean ghiNhoDangNhap = false;

    @Column(name = "thiet_bi", length = 255)
    private String thietBi;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @Column(name = "thoi_gian_het_han", nullable = false)
    private LocalDateTime thoiGianHetHan;
}
