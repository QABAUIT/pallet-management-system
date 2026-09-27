package com.pallet.backend.repository;

import com.pallet.backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    Optional<KhachHang> findByMaKh(String maKh);
    List<KhachHang> findByTrangThai(String trangThai);
    List<KhachHang> findByCreatedBy_Id(Long createdById);
}