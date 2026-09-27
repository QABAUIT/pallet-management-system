package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_cong_ty", nullable = false, length = 255)
    private String tenCongTy;

    @Column(name = "mst", nullable = false, length = 20)
    private String mst;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "sdt", length = 100)
    private String sdt;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "so_tai_khoan_ngan_hang", length = 50)
    private String soTaiKhoanNganHang;

    @Column(name = "ten_ngan_hang", length = 150)
    private String tenNganHang;

    @Column(name = "chu_tai_khoan", length = 255)
    private String chuTaiKhoan;

    @Column(name = "nguoi_dai_dien_phap_ly", length = 150)
    private String nguoiDaiDienPhapLy;

    @Column(name = "ty_le_vat_mac_dinh", nullable = false)
    private BigDecimal tyLeVatMacDinh;

}