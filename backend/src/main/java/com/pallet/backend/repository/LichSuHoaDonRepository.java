package com.pallet.backend.repository;

import com.pallet.backend.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long>, JpaSpecificationExecutor<LichSuHoaDon> {

    List<LichSuHoaDon> findByHoaDonIdOrderByThoiGianDesc(Long hoaDonId);
}
