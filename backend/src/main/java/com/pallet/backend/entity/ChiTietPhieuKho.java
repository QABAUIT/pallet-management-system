package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_phieu_kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietPhieuKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_kho_id", nullable = false)
    private PhieuKho phieuKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_id")
    private LoHang lo;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
