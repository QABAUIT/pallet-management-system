package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_dich_vu_thao_do")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuDichVuThaoDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_tiep_nhan_id", nullable = false)
    private NhanVien nhanVienTiepNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "so_pallet_nhan", nullable = false)
    private int soPalletNhan;

    @Column(name = "don_gia_dich_vu", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGiaDichVu;

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

    @Column(name = "phi_dich_vu", precision = 18, scale = 2)
    private BigDecimal phiDichVu;

    @Column(name = "ngay_tiep_nhan", nullable = false)
    private LocalDate ngayTiepNhan;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "thoi_gian_duyet")
    private LocalDateTime thoiGianDuyet;

    @Column(name = "thoi_gian_tra_hang")
    private LocalDateTime thoiGianTraHang;

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
        if (ngayTiepNhan == null) ngayTiepNhan = LocalDate.now();
        if (trangThai == null) trangThai = "da_tiep_nhan";
    }
}