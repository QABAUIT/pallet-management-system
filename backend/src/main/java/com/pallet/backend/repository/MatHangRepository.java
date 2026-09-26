package com.pallet.backend.repository;

import com.pallet.backend.entity.MatHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MatHangRepository extends JpaRepository<MatHang, Long>, JpaSpecificationExecutor<MatHang> {

    Optional<MatHang> findByMaMatHang(String maMatHang);

    boolean existsByMaMatHang(String maMatHang);

    List<MatHang> findByTrangThai(String trangThai);

    List<MatHang> findByLoaiMatHang(String loaiMatHang);

    List<MatHang> findByNccMacDinhId(Long nccMacDinhId);
}
