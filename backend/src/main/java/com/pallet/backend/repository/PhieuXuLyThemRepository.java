package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuXuLyThem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuXuLyThemRepository extends JpaRepository<PhieuXuLyThem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT x FROM PhieuXuLyThem x WHERE x.id = :id")
    Optional<PhieuXuLyThem> findForUpdate(@Param("id") Long id);

    @Query("SELECT x FROM PhieuXuLyThem x JOIN FETCH x.kho JOIN FETCH x.nhanVienThucHien "
            + "ORDER BY x.createdAt DESC")
    List<PhieuXuLyThem> timTatCa();

    @Query("SELECT x FROM PhieuXuLyThem x JOIN FETCH x.kho JOIN FETCH x.nhanVienThucHien "
            + "WHERE x.trangThai = :trangThai ORDER BY x.createdAt DESC")
    List<PhieuXuLyThem> timTheoTrangThai(@Param("trangThai") String trangThai);
}