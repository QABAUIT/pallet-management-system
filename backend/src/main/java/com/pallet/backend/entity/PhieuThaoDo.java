package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_thao_do")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuThaoDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_tai_che_id", nullable = false)
    private PhieuTaiChe phieuTaiChe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_thuc_hien_id", nullable = false)
    private NhanVien nhanVienThucHien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "so_pallet_thao_do", nullable = false)
    private int soPalletThaoDo;

    @Column(name = "so_thanh_nguyen", nullable = false)
    private int soThanhNguyen;

    @Column(name = "so_cuc_vuong", nullable = false)
    private int soCucVuong;

    @Column(name = "so_thanh_can_xu_ly", nullable = false)
    private int soThanhCanXuLy;

    @Column(name = "so_cuc_can_xu_ly", nullable = false)
    private int soCucCanXuLy;

    @Column(name = "kg_phe_lieu", nullable = false)
    private int kgPheLieu;

    @Column(name = "ngay_thuc_hien", nullable = false)
    private LocalDate ngayThucHien;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "thoi_gian_duyet")
    private LocalDateTime thoiGianDuyet;

    @Column(name = "ly_do_huy", length = 255)
    private String lyDoHuy;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

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