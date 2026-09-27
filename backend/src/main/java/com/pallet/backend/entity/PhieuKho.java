package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuong_tien_id")
    private PhuongTien phuongTien;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 20)
    private String maPhieu;

    @Column(name = "loai_phieu", nullable = false, length = 20)
    private String loaiPhieu;

    @Column(name = "bien_so_xe_doi_tac", length = 20)
    private String bienSoXeDoiTac;

    @Column(name = "so_bien_ban_kiem_tra", length = 50)
    private String soBienBanKiemTra;

    @Column(name = "ngay_gio", nullable = false)
    private LocalDateTime ngayGio;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

}