package com.pallet.backend.repository;

import com.pallet.backend.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    List<HoaDon> findByTrangThai(String trangThai);
    List<HoaDon> findByKhachHang_Id(Long khachHangId);
    List<HoaDon> findByNcc_Id(Long nccId);
    List<HoaDon> findByKho_Id(Long khoId);
    List<HoaDon> findByNhanVienLap_Id(Long nhanVienLapId);
}