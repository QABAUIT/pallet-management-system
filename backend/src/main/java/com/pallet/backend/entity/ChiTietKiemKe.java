package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_kiem_id")
    private NhanVien nguoiKiem;

    @Column(name = "ton_he_thong", nullable = false)
    private Integer tonHeThong;

    @Column(name = "ton_thuc_te")
    private Integer tonThucTe;

    // GENERATED COLUMN ở DB (V1 mới) - Postgres tự tính = ton_thuc_te - ton_he_thong,
    // không được ghi giá trị vào đây. Khi tạo/sửa dòng kiểm kê chỉ cần set tonThucTe,
    // chenhLech sẽ tự có sau khi save() + load lại từ DB (gọi findById hoặc refresh entity).
    @Column(name = "chenh_lech", insertable = false, updatable = false)
    private Integer chenhLech;

    @Column(name = "trang_thai_kiem_ke", nullable = false, length = 20)
    private String trangThaiKiemKe;

    @Column(name = "ghi_chu_giai_trinh", length = 255)
    private String ghiChuGiaiTrinh;

}