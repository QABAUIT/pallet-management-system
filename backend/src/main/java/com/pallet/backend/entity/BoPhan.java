package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bo_phan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoPhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_bo_phan", nullable = false, unique = true, length = 150)
    private String tenBoPhan;
}
