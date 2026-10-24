package com.pallet.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "phieu_tai_che")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuTaiChe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_hang_id", nullable = false)
    private LoHang loHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_thuc_hien_id", nullable = false)
    private NhanVien nhanVienThucHien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "so_luong_da_xu_ly", nullable = false)
    private Integer soLuongDaXuLy;

    // ----- kết quả phân loại 3 nhóm (cộng lại phải bằng soLuongDaXuLy) -----
    @Builder.Default
    @Column(name = "so_ban_lai", nullable = false)
    private Integer soBanLai = 0;

    @Builder.Default
    @Column(name = "so_thao_do", nullable = false)
    private Integer soThaoDo = 0;

    @Builder.Default
    @Column(name = "so_loai_bo", nullable = false)
    private Integer soLoaiBo = 0;

    @Builder.Default
    @Column(name = "kg_phe_lieu_loai_bo", nullable = false)
    private Integer kgPheLieuLoaiBo = 0;

    @Column(name = "ngay_thuc_hien", nullable = false)
    private LocalDate ngayThucHien;

    @Column(name = "thoi_gian_duyet")
    private LocalDateTime thoiGianDuyet;

    // nhap | da_phan_loai | da_hoan_tat | da_huy
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ly_do_huy", length = 255)
    private String lyDoHuy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (ngayThucHien == null) ngayThucHien = LocalDate.now();
        if (trangThai == null) trangThai = "nhap";
    }

}