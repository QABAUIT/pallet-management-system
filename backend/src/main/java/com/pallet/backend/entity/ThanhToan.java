package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xu_ly_id")
    private NhanVien nguoiXuLy;

    @Column(name = "phuong_thuc", nullable = false, length = 20)
    private String phuongThuc;

    @Column(name = "so_tien", nullable = false)
    private BigDecimal soTien;

    @Column(name = "ma_giao_dich_ngan_hang", length = 100)
    private String maGiaoDichNganHang;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "tu_dong_xac_nhan", nullable = false)
    private Boolean tuDongXacNhan;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

}