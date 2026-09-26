package com.pallet.backend.repository;

import com.pallet.backend.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long>, JpaSpecificationExecutor<KhachHang> {

    Optional<KhachHang> findByMaKh(String maKh);

    boolean existsByMaKh(String maKh);

    List<KhachHang> findByTrangThai(String trangThai);

    List<KhachHang> findByLoaiKh(String loaiKh);
}
