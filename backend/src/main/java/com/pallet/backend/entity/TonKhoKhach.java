package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ton_kho_khach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TonKhoKhach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;
}