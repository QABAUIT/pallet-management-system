package com.pallet.backend.repository;

import com.pallet.backend.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {

    List<ThanhToan> findByTrangThai(String trangThai);
    List<ThanhToan> findByHoaDon_Id(Long hoaDonId);
    List<ThanhToan> findByNguoiXuLy_Id(Long nguoiXuLyId);
}