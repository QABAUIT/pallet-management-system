package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * loai_yeu_cau: giam_gia_dac_biet | huy_hoa_don | khac
 * trang_thai: cho_duyet | da_duyet | tu_choi
 * doi_tuong_loai/doi_tuong_id: tham chiếu đa hình (polymorphic) tới bảng
 * nghiệp vụ liên quan (ví dụ 'hoa_don', id = HoaDon.id) - không map @ManyToOne
 * trực tiếp vì có thể trỏ tới nhiều loại bảng khác nhau.
 */
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

    @Column(name = "loai_yeu_cau", nullable = false, length = 30)
    private String loaiYeuCau;

    @Column(name = "doi_tuong_loai", nullable = false, length = 50)
    private String doiTuongLoai;

    @Column(name = "doi_tuong_id", nullable = false)
    private Long doiTuongId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_yeu_cau_id", nullable = false)
    private NhanVien nguoiYeuCau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "cho_duyet";

    @Column(name = "ly_do_yeu_cau", length = 255)
    private String lyDoYeuCau;

    @Column(name = "ly_do_xu_ly", length = 255)
    private String lyDoXuLy;

    @Column(name = "thoi_gian_yeu_cau", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime thoiGianYeuCau = LocalDateTime.now();

    @Column(name = "thoi_gian_xu_ly")
    private LocalDateTime thoiGianXuLy;
}
