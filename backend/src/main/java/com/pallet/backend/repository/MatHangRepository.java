package com.pallet.backend.repository;

import com.pallet.backend.entity.MatHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatHangRepository extends JpaRepository<MatHang, Long> {

    Optional<MatHang> findByMaMatHang(String maMatHang);
    List<MatHang> findByTrangThai(String trangThai);
    List<MatHang> findByNccMacDinh_Id(Long nccMacDinhId);
    List<MatHang> findByCreatedBy_Id(Long createdById);
}