package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vai_tro_id", nullable = false)
    private VaiTro vaiTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id")
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bo_phan_id")
    private BoPhan boPhan;

    @Column(name = "ma_nv", nullable = false, unique = true, length = 20)
    private String maNv;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "anh_dai_dien", length = 255)
    private String anhDaiDien;

    @Column(name = "chuc_vu", length = 150)
    private String chucVu;

    @Column(name = "loai_hop_dong", length = 100)
    private String loaiHopDong;

    @Column(name = "so_hop_dong", length = 50)
    private String soHopDong;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam;

    @Column(name = "ngay_nghi_viec")
    private LocalDate ngayNghiViec;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (trangThai == null) trangThai = "dang_lam_viec";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
}
}