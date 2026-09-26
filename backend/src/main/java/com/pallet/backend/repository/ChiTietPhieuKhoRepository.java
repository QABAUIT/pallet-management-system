package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietPhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChiTietPhieuKhoRepository extends JpaRepository<ChiTietPhieuKho, Long>, JpaSpecificationExecutor<ChiTietPhieuKho> {

    List<ChiTietPhieuKho> findByPhieuKhoId(Long phieuKhoId);

    List<ChiTietPhieuKho> findByLoId(Long loId);

    void deleteByPhieuKhoId(Long phieuKhoId);
}
