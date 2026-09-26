package com.pallet.backend.repository;

import com.pallet.backend.entity.BaoGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BaoGiaRepository extends JpaRepository<BaoGia, Long>, JpaSpecificationExecutor<BaoGia> {

    Optional<BaoGia> findByMaBaoGia(String maBaoGia);

    boolean existsByMaBaoGia(String maBaoGia);

    List<BaoGia> findByKhachHangId(Long khachHangId);

    List<BaoGia> findByTrangThai(String trangThai);
}
