package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * loai_mat_hang: pallet | linh_kien
 * chat_lieu: go | nhua | sat | khac
 * trang_thai: dang_kinh_doanh | ngung_kinh_doanh
 */
@Entity
@Table(name = "mat_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_mat_hang", nullable = false, unique = true, length = 20)
    private String maMatHang;

    @Column(name = "ten_mat_hang", nullable = false, length = 200)
    private String tenMatHang;

    @Column(name = "loai_mat_hang", nullable = false, length = 20)
    @Builder.Default
    private String loaiMatHang = "pallet";

    @Column(name = "chat_lieu", length = 20)
    private String chatLieu;

    @Column(name = "kich_thuoc_dai")
    private Integer kichThuocDai;

    @Column(name = "kich_thuoc_rong")
    private Integer kichThuocRong;

    @Column(name = "kich_thuoc_cao")
    private Integer kichThuocCao;

    @Column(name = "tai_trong_tinh", precision = 10, scale = 2)
    private BigDecimal taiTrongTinh;

    @Column(name = "tai_trong_dong", precision = 10, scale = 2)
    private BigDecimal taiTrongDong;

    @Column(name = "tieu_chuan", length = 150)
    private String tieuChuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_mac_dinh_id")
    private NhaCungCap nccMacDinh;

    @Column(name = "don_gia_ban", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal donGiaBan = BigDecimal.ZERO;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "dang_kinh_doanh";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private NhanVien createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}
