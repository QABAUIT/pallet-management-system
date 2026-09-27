package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

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

    @Column(name = "ma_hoa_don", nullable = false, unique = true, length = 20)
    private String maHoaDon;

    @Column(name = "loai_giao_dich", nullable = false, length = 30)
    private String loaiGiaoDich;

    @Column(name = "ngay_lap", nullable = false)
    private LocalDateTime ngayLap;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "tong_tien_hang", nullable = false)
    private BigDecimal tongTienHang;

    @Column(name = "tong_tien_chiet_khau", nullable = false)
    private BigDecimal tongTienChietKhau;

    @Column(name = "ty_le_giam_gia_tong", nullable = false)
    private BigDecimal tyLeGiamGiaTong;

    @Column(name = "tien_giam_gia_tong", nullable = false)
    private BigDecimal tienGiamGiaTong;

    @Column(name = "tien_truoc_thue", nullable = false)
    private BigDecimal tienTruocThue;

    @Column(name = "phi_giao_hang", nullable = false)
    private BigDecimal phiGiaoHang;

    @Column(name = "ty_le_vat", nullable = false)
    private BigDecimal tyLeVat;

    @Column(name = "tien_vat", nullable = false)
    private BigDecimal tienVat;

    @Column(name = "tong_thanh_toan", nullable = false)
    private BigDecimal tongThanhToan;

    @Column(name = "tien_khach_dua")
    private BigDecimal tienKhachDua;

    @Column(name = "tien_thoi_lai")
    private BigDecimal tienThoiLai;

    @Column(name = "phuong_thuc_thanh_toan", length = 20)
    private String phuongThucThanhToan;

    @Column(name = "han_thanh_toan")
    private LocalDate hanThanhToan;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "da_chot_so", nullable = false)
    private Boolean daChotSo;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;

    @Column(name = "file_pdf", length = 255)
    private String filePdf;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}