package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * loai_phieu: nhap_kho | xuat_kho | chuyen_kho | dieu_chinh_kiem_ke
 * trang_thai: cho_xu_ly | da_hoan_thanh | da_huy
 * Lưu ý: khi loai_phieu = 'chuyen_kho', DB bắt buộc khoDoiUng khác NULL và
 * khác kho; các loại khác thì khoDoiUng phải NULL (xem CHECK trong schema).
 */
@Entity
@Table(name = "phieu_kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @Column(name = "loai_phieu", nullable = false, length = 20)
    private String loaiPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_doi_ung_id")
    private Kho khoDoiUng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_id")
    private ViTriLuuTru viTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id")
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_giao_nhan_id")
    private NhanVien nhanVienGiaoNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "bien_so_xe_doi_tac", length = 20)
    private String bienSoXeDoiTac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuong_tien_id")
    private PhuongTien phuongTien;

    @Column(name = "so_bien_ban_kiem_tra", length = 50)
    private String soBienBanKiemTra;

    @Column(name = "ngay_gio", nullable = false)
    @Builder.Default
    private LocalDateTime ngayGio = LocalDateTime.now();

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "cho_xu_ly";
}
