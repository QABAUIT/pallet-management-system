package com.pallet.backend.repository;

import com.pallet.backend.entity.PhienKiemKe;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhienKiemKeRepository extends JpaRepository<PhienKiemKe, Long> {

    Optional<PhienKiemKe> findByMaPhien(String maPhien);
    List<PhienKiemKe> findByTrangThai(String trangThai);
    List<PhienKiemKe> findByKho_Id(Long khoId);
    List<PhienKiemKe> findByNguoiTao_Id(Long nguoiTaoId);
    List<PhienKiemKe> findByNguoiChot_Id(Long nguoiChotId);

    boolean existsByKho_IdAndTrangThai(Long khoId, String trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhienKiemKe p WHERE p.id = :id")
    Optional<PhienKiemKe> findForUpdate(@Param("id") Long id);
}
