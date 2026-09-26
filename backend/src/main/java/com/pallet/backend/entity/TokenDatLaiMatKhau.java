package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_dat_lai_mat_khau")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDatLaiMatKhau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @Column(name = "thoi_gian_het_han", nullable = false)
    private LocalDateTime thoiGianHetHan;

    @Column(name = "da_su_dung", nullable = false)
    @Builder.Default
    private Boolean daSuDung = false;
}
