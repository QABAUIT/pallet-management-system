package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuDichVuThaoDo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuDichVuThaoDoRepository extends JpaRepository<PhieuDichVuThaoDo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuDichVuThaoDo p WHERE p.id = :id")
    Optional<PhieuDichVuThaoDo> findForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM PhieuDichVuThaoDo p JOIN FETCH p.khachHang JOIN FETCH p.kho "
            + "JOIN FETCH p.nhanVienTiepNhan ORDER BY p.createdAt DESC")
    List<PhieuDichVuThaoDo> timTatCa();

    @Query("SELECT p FROM PhieuDichVuThaoDo p JOIN FETCH p.khachHang JOIN FETCH p.kho "
            + "JOIN FETCH p.nhanVienTiepNhan WHERE p.trangThai = :trangThai ORDER BY p.createdAt DESC")
    List<PhieuDichVuThaoDo> timTheoTrangThai(@Param("trangThai") String trangThai);
}