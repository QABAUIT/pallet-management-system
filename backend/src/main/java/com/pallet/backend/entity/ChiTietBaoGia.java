package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;

/**
 * tien_chiet_khau và thanh_tien là GENERATED COLUMN ở DB, không set từ Java.
 */
@Entity
@Table(name = "chi_tiet_bao_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietBaoGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bao_gia_id", nullable = false)
    private BaoGia baoGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ty_le_chiet_khau", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tyLeChietKhau = BigDecimal.ZERO;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "tien_chiet_khau", insertable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal tienChietKhau;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "thanh_tien", insertable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;
}
