package com.pallet.backend.repository;

import com.pallet.backend.entity.MaQRThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaQRThanhToanRepository extends JpaRepository<MaQRThanhToan, Long> {

    List<MaQRThanhToan> findByTrangThai(String trangThai);
    List<MaQRThanhToan> findByHoaDon_Id(Long hoaDonId);
}