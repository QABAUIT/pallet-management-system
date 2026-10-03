package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

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

    @Column(name = "don_gia", nullable = false)
    private BigDecimal donGia;

    @Column(name = "ty_le_chiet_khau", nullable = false)
    private BigDecimal tyLeChietKhau;

    // GENERATED COLUMN ở DB (V1 mới) - Postgres tự tính, không được ghi giá trị vào đây.
    @Column(name = "tien_chiet_khau", insertable = false, updatable = false)
    private BigDecimal tienChietKhau;

    // GENERATED COLUMN ở DB (V1 mới) - Postgres tự tính, không được ghi giá trị vào đây.
    @Column(name = "thanh_tien", insertable = false, updatable = false)
    private BigDecimal thanhTien;

}