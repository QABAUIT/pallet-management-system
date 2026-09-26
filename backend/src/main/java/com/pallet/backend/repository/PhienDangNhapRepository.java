package com.pallet.backend.repository;

import com.pallet.backend.entity.PhienDangNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhienDangNhapRepository extends JpaRepository<PhienDangNhap, Long>, JpaSpecificationExecutor<PhienDangNhap> {

    Optional<PhienDangNhap> findByRefreshToken(String refreshToken);

    List<PhienDangNhap> findByNhanVienId(Long nhanVienId);

    void deleteByNhanVienId(Long nhanVienId);

    void deleteByThoiGianHetHanBefore(LocalDateTime moc);
}
