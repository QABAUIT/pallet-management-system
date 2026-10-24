package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "lo_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id")
    private NhaCungCap ncc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_don_mua_id")
    private ChiTietDonMua chiTietDonMua;

    @Column(name = "ma_lo", nullable = false, unique = true, length = 30)
    private String maLo;

    @Column(name = "so_luong_nhap", nullable = false)
    private Integer soLuongNhap;

    @Column(name = "so_luong_con_lai", nullable = false)
    private Integer soLuongConLai;

    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate ngayNhap;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "muc_dich_nhap", nullable = false, length = 20)
    private String mucDichNhap;

    // Giá nhập của riêng lô này (null với lô cũ nhập tay trước khi có đơn mua)
    @Column(name = "don_gia_nhap")
    private BigDecimal donGiaNhap;

}