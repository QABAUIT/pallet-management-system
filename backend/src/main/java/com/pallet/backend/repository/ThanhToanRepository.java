package com.pallet.backend.repository;

import com.pallet.backend.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long>, JpaSpecificationExecutor<ThanhToan> {

    List<ThanhToan> findByHoaDonId(Long hoaDonId);

    List<ThanhToan> findByTrangThai(String trangThai);

    List<ThanhToan> findByHoaDonIdAndTrangThai(Long hoaDonId, String trangThai);
}
