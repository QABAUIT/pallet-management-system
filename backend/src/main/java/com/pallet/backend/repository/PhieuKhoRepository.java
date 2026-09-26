package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PhieuKhoRepository extends JpaRepository<PhieuKho, Long>, JpaSpecificationExecutor<PhieuKho> {

    Optional<PhieuKho> findByMaPhieu(String maPhieu);

    boolean existsByMaPhieu(String maPhieu);

    Page<PhieuKho> findByKhoIdAndTrangThai(Long khoId, String trangThai, Pageable pageable);

    List<PhieuKho> findByHoaDonId(Long hoaDonId);

    List<PhieuKho> findByLoaiPhieuAndTrangThai(String loaiPhieu, String trangThai);
}
