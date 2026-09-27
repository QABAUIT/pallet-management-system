package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quan_ly_id")
    private NhanVien quanLy;

    @Column(name = "ma_kho", nullable = false, unique = true, length = 20)
    private String maKho;

    @Column(name = "ten_kho", nullable = false, length = 150)
    private String tenKho;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "loai_dia_diem", nullable = false, length = 20)
    private String loaiDiaDiem;

    @Column(name = "dien_tich_m2")
    private BigDecimal dienTichM2;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

}