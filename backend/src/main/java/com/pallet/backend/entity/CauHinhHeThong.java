package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "cau_hinh_he_thong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauHinhHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_cap_nhat_id")
    private NhanVien nguoiCapNhat;

    @Column(name = "khoa", nullable = false, unique = true, length = 100)
    private String khoa;

    @Column(name = "gia_tri", nullable = false, length = 255)
    private String giaTri;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}