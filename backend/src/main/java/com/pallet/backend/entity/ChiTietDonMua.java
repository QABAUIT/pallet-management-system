package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "chi_tiet_don_mua")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDonMua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_mua_id", nullable = false)
    private DonMua donMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong_dat", nullable = false)
    private Integer soLuongDat;

    @Column(name = "so_luong_da_nhan", nullable = false)
    private Integer soLuongDaNhan;

    @Column(name = "don_gia", nullable = false)
    private BigDecimal donGia;

    // cột GENERATED ở DB: chỉ đọc, không ghi
    @Column(name = "thanh_tien", insertable = false, updatable = false)
    private BigDecimal thanhTien;

    @PrePersist
    void onCreate() {
        if (soLuongDaNhan == null) soLuongDaNhan = 0;
    }
}