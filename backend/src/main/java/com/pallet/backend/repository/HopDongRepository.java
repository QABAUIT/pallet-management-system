package com.pallet.backend.repository;

import com.pallet.backend.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HopDongRepository extends JpaRepository<HopDong, Long> {

    Optional<HopDong> findByMaHopDong(String maHopDong);
    List<HopDong> findByTrangThai(String trangThai);
    List<HopDong> findByKhachHang_Id(Long khachHangId);
    List<HopDong> findByNcc_Id(Long nccId);
    List<HopDong> findByNguoiTao_Id(Long nguoiTaoId);
}