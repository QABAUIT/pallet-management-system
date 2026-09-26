package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * phuong_thuc: tien_mat | chuyen_khoan | qr_code
 * trang_thai: cho_xu_ly | thanh_cong | that_bai
 */
@Entity
@Table(name = "thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "phuong_thuc", nullable = false, length = 20)
    private String phuongThuc;

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ma_giao_dich_ngan_hang", length = 100)
    private String maGiaoDichNganHang;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "cho_xu_ly";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xu_ly_id")
    private NhanVien nguoiXuLy;

    @Column(name = "tu_dong_xac_nhan", nullable = false)
    @Builder.Default
    private Boolean tuDongXacNhan = false;

    @Column(name = "thoi_gian", nullable = false)
    @Builder.Default
    private LocalDateTime thoiGian = LocalDateTime.now();

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
