package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * loai_doi_tac: khach_hang | nha_cung_cap
 * trang_thai: du_thao | hieu_luc | het_han | da_thanh_ly
 */
@Entity
@Table(name = "hop_dong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hop_dong", nullable = false, unique = true, length = 30)
    private String maHopDong;

    @Column(name = "loai_doi_tac", nullable = false, length = 20)
    private String loaiDoiTac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id")
    private NhaCungCap ncc;

    @Column(name = "ngay_ky")
    private LocalDate ngayKy;

    @Column(name = "ngay_hieu_luc")
    private LocalDate ngayHieuLuc;

    @Column(name = "ngay_het_han")
    private LocalDate ngayHetHan;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "file_dinh_kem", length = 255)
    private String fileDinhKem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id", nullable = false)
    private NhanVien nguoiTao;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "du_thao";
}
