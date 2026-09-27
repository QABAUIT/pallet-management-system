package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "phe_duyet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PheDuyet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_yeu_cau_id", nullable = false)
    private NhanVien nguoiYeuCau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "loai_yeu_cau", nullable = false, length = 30)
    private String loaiYeuCau;

    @Column(name = "doi_tuong_loai", nullable = false, length = 50)
    private String doiTuongLoai;

    @Column(name = "doi_tuong_id", nullable = false)
    private Long doiTuongId;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ly_do_yeu_cau", length = 255)
    private String lyDoYeuCau;

    @Column(name = "ly_do_xu_ly", length = 255)
    private String lyDoXuLy;

    @Column(name = "thoi_gian_yeu_cau", nullable = false)
    private LocalDateTime thoiGianYeuCau;

    @Column(name = "thoi_gian_xu_ly")
    private LocalDateTime thoiGianXuLy;

}