package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * loai_giao_dich: thu_ban_pallet | thu_ban_linh_kien | thu_khac |
 *                 chi_mua_pallet_cu | chi_nguyen_lieu_phu_tro | chi_sua_chua |
 *                 chi_van_chuyen | chi_khac
 * trang_thai: nhap | cho_duyet | da_thanh_toan | qua_han | da_huy
 * phuong_thuc_thanh_toan: tien_mat | chuyen_khoan | qr_code
 *
 * Lưu ý: hóa đơn đã da_chot_so = true bị DB chặn UPDATE/DELETE qua trigger
 * (xem fn_block_edit_hoadon_chotso trong schema).
 */
@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hoa_don", nullable = false, unique = true, length = 20)
    private String maHoaDon;

    @Column(name = "loai_giao_dich", nullable = false, length = 30)
    private String loaiGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id")
    private NhaCungCap ncc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id")
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_lap_id", nullable = false)
    private NhanVien nhanVienLap;

    @Column(name = "ngay_lap", nullable = false)
    @Builder.Default
    private LocalDateTime ngayLap = LocalDateTime.now();

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "nhap";

    @Column(name = "tong_tien_hang", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tongTienHang = BigDecimal.ZERO;

    @Column(name = "tong_tien_chiet_khau", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tongTienChietKhau = BigDecimal.ZERO;

    @Column(name = "ty_le_giam_gia_tong", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tyLeGiamGiaTong = BigDecimal.ZERO;

    @Column(name = "tien_giam_gia_tong", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tienGiamGiaTong = BigDecimal.ZERO;

    @Column(name = "tien_truoc_thue", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tienTruocThue = BigDecimal.ZERO;

    @Column(name = "phi_giao_hang", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal phiGiaoHang = BigDecimal.ZERO;

    @Column(name = "ty_le_vat", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tyLeVat = new BigDecimal("8");

    @Column(name = "tien_vat", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tienVat = BigDecimal.ZERO;

    @Column(name = "tong_thanh_toan", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal tongThanhToan = BigDecimal.ZERO;

    @Column(name = "tien_khach_dua", precision = 18, scale = 2)
    private BigDecimal tienKhachDua;

    @Column(name = "tien_thoi_lai", precision = 18, scale = 2)
    private BigDecimal tienThoiLai;

    @Column(name = "phuong_thuc_thanh_toan", length = 20)
    private String phuongThucThanhToan;

    @Column(name = "han_thanh_toan")
    private LocalDate hanThanhToan;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "da_chot_so", nullable = false)
    @Builder.Default
    private Boolean daChotSo = false;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "file_pdf", length = 255)
    private String filePdf;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}
