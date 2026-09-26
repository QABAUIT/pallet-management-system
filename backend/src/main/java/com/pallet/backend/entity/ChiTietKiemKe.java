package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

/**
 * chenh_lech là GENERATED COLUMN ở DB (= ton_thuc_te - ton_he_thong),
 * KHÔNG set từ tầng Java. trang_thai_kiem_ke: khop | lech | cho_kiem
 * (DB có CHECK bắt buộc khớp với kết quả tính chenh_lech).
 */
@Entity
@Table(name = "chi_tiet_kiem_ke")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietKiemKe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phien_kiem_ke_id", nullable = false)
    private PhienKiemKe phienKiemKe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_id")
    private ViTriLuuTru viTri;

    @Column(name = "ton_he_thong", nullable = false)
    private Integer tonHeThong;

    @Column(name = "ton_thuc_te")
    private Integer tonThucTe;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "chenh_lech", insertable = false, updatable = false)
    private Integer chenhLech;

    @Column(name = "trang_thai_kiem_ke", nullable = false, length = 20)
    @Builder.Default
    private String trangThaiKiemKe = "cho_kiem";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_kiem_id")
    private NhanVien nguoiKiem;

    @Column(name = "ghi_chu_giai_trinh", length = 255)
    private String ghiChuGiaiTrinh;
}
