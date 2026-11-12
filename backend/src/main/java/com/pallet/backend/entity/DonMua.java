package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "don_mua")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonMua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_don_mua", nullable = false, unique = true, length = 20)
    private String maDonMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id", nullable = false)
    private NhaCungCap ncc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_lap_id", nullable = false)
    private NhanVien nhanVienLap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xac_nhan_id")
    private NhanVien nguoiXacNhan;

    @Column(name = "ngay_dat", nullable = false)
    private LocalDate ngayDat;

    @Column(name = "ngay_du_kien_nhan")
    private LocalDate ngayDuKienNhan;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "tong_tien", nullable = false)
    private BigDecimal tongTien;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ly_do_huy", length = 255)
    private String lyDoHuy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (ngayDat == null) ngayDat = LocalDate.now();
        if (trangThai == null) trangThai = "nhap";
        if (tongTien == null) tongTien = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}