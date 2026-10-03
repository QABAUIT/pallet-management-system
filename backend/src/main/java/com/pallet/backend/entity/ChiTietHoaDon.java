package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "chi_tiet_hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", nullable = false)
    private BigDecimal donGia;

    @Column(name = "ty_le_chiet_khau", nullable = false)
    private BigDecimal tyLeChietKhau;

    // GENERATED COLUMN ở DB (V1 mới) - Postgres tự tính = so_luong * don_gia * ty_le_chiet_khau / 100,
    // KHÔNG được phép ghi giá trị vào đây, nếu không sẽ lỗi khi INSERT/UPDATE.
    // nullable=true vì cột generated trong V1 không khai NOT NULL.
    @Column(name = "tien_chiet_khau", insertable = false, updatable = false)
    private BigDecimal tienChietKhau;

    // GENERATED COLUMN ở DB (V1 mới) - Postgres tự tính = so_luong*don_gia - tien_chiet_khau.
    // Đọc được bình thường sau khi save() (gọi lại repository.findById/refresh để lấy giá trị DB vừa tính),
    // nhưng tuyệt đối không set() giá trị này trước khi save.
    @Column(name = "thanh_tien", insertable = false, updatable = false)
    private BigDecimal thanhTien;

}