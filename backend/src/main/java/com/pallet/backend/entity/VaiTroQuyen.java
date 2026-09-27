package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vai_tro_quyen", uniqueConstraints = {@UniqueConstraint(columnNames = {"vai_tro_id", "module_code"})})
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
    private Boolean duocXem;

    @Column(name = "duoc_them", nullable = false)
    private Boolean duocThem;

    @Column(name = "duoc_sua", nullable = false)
    private Boolean duocSua;

    @Column(name = "duoc_xoa", nullable = false)
    private Boolean duocXoa;

    @Column(name = "duoc_duyet", nullable = false)
    private Boolean duocDuyet;

}