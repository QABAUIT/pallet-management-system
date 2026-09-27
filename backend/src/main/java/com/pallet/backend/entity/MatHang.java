package com.pallet.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_mac_dinh_id")
    private NhaCungCap nccMacDinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private NhanVien createdBy;

    @Column(name = "ma_mat_hang", nullable = false, unique = true, length = 20)
    private String maMatHang;

    @Column(name = "ten_mat_hang", nullable = false, length = 200)
    private String tenMatHang;

    @Column(name = "loai_mat_hang", nullable = false, length = 20)
    private String loaiMatHang;

    @Column(name = "chat_lieu", length = 20)
    private String chatLieu;

    @Column(name = "kich_thuoc_dai")
    private Integer kichThuocDai;

    @Column(name = "kich_thuoc_rong")
    private Integer kichThuocRong;

    @Column(name = "kich_thuoc_cao")
    private Integer kichThuocCao;

    @Column(name = "tai_trong_tinh")
    private BigDecimal taiTrongTinh;

    @Column(name = "tai_trong_dong")
    private BigDecimal taiTrongDong;

    @Column(name = "tieu_chuan", length = 150)
    private String tieuChuan;

    @Column(name = "don_gia_ban", nullable = false)
    private BigDecimal donGiaBan;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String moTa;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}