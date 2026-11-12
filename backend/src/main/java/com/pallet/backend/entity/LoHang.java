package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

/**
 * Lô hàng theo dõi số lượng nhập và số lượng còn lại của một lần nhập.
 *
 * <p>Quy ước: {@code soLuongConLai} chỉ được giảm khi duyệt phân loại
 * ({@code PhieuTaiCheServiceImpl.duyetPhanLoai}) và khi xuất kho thủ công có chọn lô
 * ({@code PhieuXuatServiceImpl.ghiChiTietVaTruTon}). Giá trị này <b>không phản ánh tồn kho</b>
 * sau khi bán, tháo dỡ hoặc đóng pallet; tồn kho thực tế được theo dõi riêng trong
 * {@code ton_kho}.</p>
 *
 * <p>Ngoại lệ: khi hủy phiếu nhập thủ công, lô bị đặt về 0; khi sửa phiếu nhập, lô cũ
 * bị xóa cùng chi tiết phiếu.</p>
 */
@Entity
@Table(name = "lo_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mat_hang_id", nullable = false)
    private MatHang matHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ncc_id")
    private NhaCungCap ncc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_don_mua_id")
    private ChiTietDonMua chiTietDonMua;

    @Column(name = "ma_lo", nullable = false, unique = true, length = 30)
    private String maLo;

    @Column(name = "so_luong_nhap", nullable = false)
    private Integer soLuongNhap;

    /**
     * Số lượng còn lại của lô theo quy ước nghiệp vụ (không phải tồn kho thực tế).
     * Chỉ giảm khi duyệt phân loại hoặc xuất kho thủ công có chọn lô.
     */
    @Column(name = "so_luong_con_lai", nullable = false)
    private Integer soLuongConLai;

    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate ngayNhap;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "muc_dich_nhap", nullable = false, length = 20)
    private String mucDichNhap;

    // Giá nhập của riêng lô này (null với lô cũ nhập tay trước khi có đơn mua)
    @Column(name = "don_gia_nhap")
    private BigDecimal donGiaNhap;

}