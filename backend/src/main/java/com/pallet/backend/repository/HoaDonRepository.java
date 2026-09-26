package com.pallet.backend.repository;

import com.pallet.backend.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    boolean existsByMaHoaDon(String maHoaDon);

    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);

    List<HoaDon> findByKhachHangId(Long khachHangId);

    List<HoaDon> findByNccId(Long nccId);

    List<HoaDon> findByKhoId(Long khoId);
}
