package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private NhanVien createdBy;

    @Column(name = "ma_kh", nullable = false, unique = true, length = 20)
    private String maKh;

    @Column(name = "ten_kh", nullable = false, length = 200)
    private String tenKh;

    @Column(name = "loai_kh", length = 20)
    private String loaiKh;

    @Column(name = "mst", length = 20)
    private String mst;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "nguoi_dai_dien", length = 150)
    private String nguoiDaiDien;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}