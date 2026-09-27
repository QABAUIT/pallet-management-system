package com.pallet.backend.repository;

import com.pallet.backend.entity.BangGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BangGiaRepository extends JpaRepository<BangGia, Long> {

    List<BangGia> findByMatHang_Id(Long matHangId);
    List<BangGia> findByKhachHang_Id(Long khachHangId);
}