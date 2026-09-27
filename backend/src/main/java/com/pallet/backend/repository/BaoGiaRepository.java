package com.pallet.backend.repository;

import com.pallet.backend.entity.BaoGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaoGiaRepository extends JpaRepository<BaoGia, Long> {

    Optional<BaoGia> findByMaBaoGia(String maBaoGia);
    List<BaoGia> findByTrangThai(String trangThai);
    List<BaoGia> findByKhachHang_Id(Long khachHangId);
    List<BaoGia> findByNguoiTao_Id(Long nguoiTaoId);
}