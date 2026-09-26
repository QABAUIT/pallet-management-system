package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * hanh_dong: tao_moi | cap_nhat | duyet | huy | xoa_dong_hang
 */
@Entity
@Table(name = "lich_su_hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_sua_id", nullable = false)
    private NhanVien nguoiSua;

    @Column(name = "thoi_gian", nullable = false)
    @Builder.Default
    private LocalDateTime thoiGian = LocalDateTime.now();

    @Column(name = "hanh_dong", nullable = false, length = 30)
    private String hanhDong;

    @Column(name = "truong_thay_doi", length = 100)
    private String truongThayDoi;

    @Column(name = "gia_tri_cu", columnDefinition = "TEXT")
    private String giaTriCu;

    @Column(name = "gia_tri_moi", columnDefinition = "TEXT")
    private String giaTriMoi;

    @Column(name = "ly_do", length = 255)
    private String lyDo;
}
