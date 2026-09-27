package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "ton_kho", uniqueConstraints = {@UniqueConstraint(columnNames = {"mat_hang_id", "kho_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TonKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_id")
    private ViTriLuuTru viTri;

    @Column(name = "so_luong_ton_kho", nullable = false)
    private Integer soLuongTonKho;

    @Column(name = "so_luong_da_giu_cho", nullable = false)
    private Integer soLuongDaGiuCho;

    @Column(name = "so_luong_toi_thieu", nullable = false)
    private Integer soLuongToiThieu;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}