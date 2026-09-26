package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long>, JpaSpecificationExecutor<ChiTietHoaDon> {

    List<ChiTietHoaDon> findByHoaDonId(Long hoaDonId);

    void deleteByHoaDonId(Long hoaDonId);
}
