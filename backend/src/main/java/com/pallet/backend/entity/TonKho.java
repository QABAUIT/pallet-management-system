package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ton_kho",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mat_hang_id", "kho_id"}))
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
    @Builder.Default
    private Integer soLuongTonKho = 0;

    @Column(name = "so_luong_da_giu_cho", nullable = false)
    @Builder.Default
    private Integer soLuongDaGiuCho = 0;

    @Column(name = "so_luong_toi_thieu", nullable = false)
    @Builder.Default
    private Integer soLuongToiThieu = 0;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}
