package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietPhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietPhieuKhoRepository extends JpaRepository<ChiTietPhieuKho, Long> {

    List<ChiTietPhieuKho> findByPhieuKho_Id(Long phieuKhoId);
    List<ChiTietPhieuKho> findByMatHang_Id(Long matHangId);
    List<ChiTietPhieuKho> findByLo_Id(Long loId);
}