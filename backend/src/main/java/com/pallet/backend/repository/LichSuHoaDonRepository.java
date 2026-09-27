package com.pallet.backend.repository;

import com.pallet.backend.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long> {

    List<LichSuHoaDon> findByHoaDon_Id(Long hoaDonId);
    List<LichSuHoaDon> findByNguoiSua_Id(Long nguoiSuaId);
}