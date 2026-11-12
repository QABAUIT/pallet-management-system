package com.pallet.backend.repository;

import com.pallet.backend.entity.DinhMuc;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DinhMucRepository extends JpaRepository<DinhMuc, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DinhMuc d WHERE d.id = :id")
    Optional<DinhMuc> findForUpdate(@Param("id") Long id);

    @Query("SELECT d FROM DinhMuc d JOIN FETCH d.matHangThanhPham ORDER BY d.id")
    List<DinhMuc> timTatCa();

    @Query("SELECT d FROM DinhMuc d JOIN FETCH d.matHangThanhPham WHERE d.trangThai = 'hoat_dong' ORDER BY d.id")
    List<DinhMuc> timHoatDong();
}