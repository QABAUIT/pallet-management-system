package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vai_tro_quyen",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vai_tro_id", "module_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTroQuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vai_tro_id", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "duoc_xem", nullable = false)
    @Builder.Default
    private Boolean duocXem = false;

    @Column(name = "duoc_them", nullable = false)
    @Builder.Default
    private Boolean duocThem = false;

    @Column(name = "duoc_sua", nullable = false)
    @Builder.Default
    private Boolean duocSua = false;

    @Column(name = "duoc_xoa", nullable = false)
    @Builder.Default
    private Boolean duocXoa = false;

    @Column(name = "duoc_duyet", nullable = false)
    @Builder.Default
    private Boolean duocDuyet = false;
}
