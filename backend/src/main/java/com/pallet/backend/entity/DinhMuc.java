package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dinh_muc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DinhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_dinh_muc", nullable = false, unique = true, length = 20)
    private String maDinhMuc;

    @Column(name = "ten_dinh_muc", nullable = false, length = 150)
    private String tenDinhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_thanh_pham_id", nullable = false)
    private MatHang matHangThanhPham;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void truocKhiLuu() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (trangThai == null) trangThai = "hoat_dong";
    }
}