package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bắt buộc phải có ít nhất một trong 2: nguoiNhan hoặc vaiTroNhan (DB CHECK).
 */
@Entity
@Table(name = "thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loai_thong_bao", nullable = false, length = 50)
    private String loaiThongBao;

    @Column(name = "tieu_de", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhan_id")
    private NhanVien nguoiNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vai_tro_nhan_id")
    private VaiTro vaiTroNhan;

    @Column(name = "doi_tuong_loai", length = 50)
    private String doiTuongLoai;

    @Column(name = "doi_tuong_id")
    private Long doiTuongId;

    @Column(name = "da_doc", nullable = false)
    @Builder.Default
    private Boolean daDoc = false;

    @Column(name = "thoi_gian_tao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();
}
