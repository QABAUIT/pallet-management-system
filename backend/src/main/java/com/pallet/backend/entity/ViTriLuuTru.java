package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vi_tri_luu_tru")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViTriLuuTru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @Column(name = "khu_vuc", nullable = false, length = 20)
    private String khuVuc;

    @Column(name = "ke", length = 20)
    private String ke;

    @Column(name = "tang", length = 20)
    private String tang;

    @Column(name = "ma_vi_tri", nullable = false, unique = true, length = 30)
    private String maViTri;
}
