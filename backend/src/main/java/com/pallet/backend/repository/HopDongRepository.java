package com.pallet.backend.repository;

import com.pallet.backend.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface HopDongRepository extends JpaRepository<HopDong, Long>, JpaSpecificationExecutor<HopDong> {

    Optional<HopDong> findByMaHopDong(String maHopDong);

    boolean existsByMaHopDong(String maHopDong);

    List<HopDong> findByKhachHangId(Long khachHangId);

    List<HopDong> findByNccId(Long nccId);

    List<HopDong> findByTrangThai(String trangThai);
}
