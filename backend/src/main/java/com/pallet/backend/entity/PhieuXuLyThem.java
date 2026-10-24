package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_xu_ly_them")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuXuLyThem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_thuc_hien_id", nullable = false)
    private NhanVien nhanVienThucHien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "loai_nguon", nullable = false, length = 10)
    private String loaiNguon;

    @Column(name = "so_dat_lai", nullable = false)
    private int soDatLai;

    @Column(name = "so_phe_lieu", nullable = false)
    private int soPheLieu;

    @Column(name = "kg_phe_lieu", nullable = false)
    private int kgPheLieu;

    @Column(name = "ly_do", nullable = false, length = 255)
    private String lyDo;

    @Column(name = "ngay_thuc_hien", nullable = false)
    private LocalDate ngayThucHien;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "thoi_gian_duyet")
    private LocalDateTime thoiGianDuyet;

    @Column(name = "ly_do_huy", length = 255)
    private String lyDoHuy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void truocKhiLuu() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (ngayThucHien == null) ngayThucHien = LocalDate.now();
        if (trangThai == null) trangThai = "nhap";
    }
}