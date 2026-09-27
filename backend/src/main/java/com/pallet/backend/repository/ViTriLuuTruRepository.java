package com.pallet.backend.repository;

import com.pallet.backend.entity.ViTriLuuTru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViTriLuuTruRepository extends JpaRepository<ViTriLuuTru, Long> {

    Optional<ViTriLuuTru> findByMaViTri(String maViTri);
    List<ViTriLuuTru> findByKho_Id(Long khoId);
}