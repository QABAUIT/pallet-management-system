package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_dinh_muc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDinhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinh_muc_id", nullable = false)
    private DinhMuc dinhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;
}