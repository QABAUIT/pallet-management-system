package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietBaoGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietBaoGiaRepository extends JpaRepository<ChiTietBaoGia, Long> {

    List<ChiTietBaoGia> findByBaoGia_Id(Long baoGiaId);
    List<ChiTietBaoGia> findByMatHang_Id(Long matHangId);
}