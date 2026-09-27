package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long> {

    List<ChiTietHoaDon> findByHoaDon_Id(Long hoaDonId);
    List<ChiTietHoaDon> findByMatHang_Id(Long matHangId);
}