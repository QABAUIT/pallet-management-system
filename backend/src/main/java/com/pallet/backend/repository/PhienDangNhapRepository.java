package com.pallet.backend.repository;

import com.pallet.backend.entity.PhienDangNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhienDangNhapRepository extends JpaRepository<PhienDangNhap, Long> {

    List<PhienDangNhap> findByNhanVien_Id(Long nhanVienId);

    Optional<PhienDangNhap> findByRefreshToken(String refreshToken);

    void deleteByNhanVien_Id(Long nhanVienId);
}