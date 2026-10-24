package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chi_tiet_dong_pallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietDongPallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dong_pallet_id", nullable = false)
    private PhieuDongPallet phieuDongPallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @Column(name = "so_luong_dinh_muc", nullable = false)
    private int soLuongDinhMuc;

    @Column(name = "so_luong_thuc_te", nullable = false)
    private int soLuongThucTe;

    @Column(name = "chenh_lech", insertable = false, updatable = false)
    private Integer chenhLech;

    @Column(name = "ly_do_chenh_lech", length = 255)
    private String lyDoChenhLech;
}