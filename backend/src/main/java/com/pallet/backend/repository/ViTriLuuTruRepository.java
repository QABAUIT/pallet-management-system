package com.pallet.backend.repository;

import com.pallet.backend.entity.ViTriLuuTru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ViTriLuuTruRepository extends JpaRepository<ViTriLuuTru, Long>, JpaSpecificationExecutor<ViTriLuuTru> {

    Optional<ViTriLuuTru> findByMaViTri(String maViTri);

    boolean existsByMaViTri(String maViTri);

    List<ViTriLuuTru> findByKhoId(Long khoId);
}
