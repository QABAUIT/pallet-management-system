package com.pallet.backend.repository;

import com.pallet.backend.entity.MaQRThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MaQRThanhToanRepository extends JpaRepository<MaQRThanhToan, Long>, JpaSpecificationExecutor<MaQRThanhToan> {

    List<MaQRThanhToan> findByHoaDonId(Long hoaDonId);

    Optional<MaQRThanhToan> findTopByHoaDonIdOrderByThoiGianTaoDesc(Long hoaDonId);

    List<MaQRThanhToan> findByTrangThai(String trangThai);
}
