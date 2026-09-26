package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * trang_thai: hoat_dong | ngung_hoat_dong | bao_tri
 */
@Entity
@Table(name = "phuong_tien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhuongTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bien_so", nullable = false, unique = true, length = 20)
    private String bienSo;

    @Column(name = "loai_xe", length = 50)
    private String loaiXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_xe_id")
    private NhanVien taiXe;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "hoat_dong";
}
