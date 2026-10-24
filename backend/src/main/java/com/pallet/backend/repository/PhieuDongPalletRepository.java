package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuDongPallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuDongPalletRepository extends JpaRepository<PhieuDongPallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuDongPallet p WHERE p.id = :id")
    Optional<PhieuDongPallet> findForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM PhieuDongPallet p JOIN FETCH p.dinhMuc JOIN FETCH p.matHangThanhPham "
            + "JOIN FETCH p.kho JOIN FETCH p.nhanVienThucHien ORDER BY p.createdAt DESC")
    List<PhieuDongPallet> timTatCa();

    @Query("SELECT p FROM PhieuDongPallet p JOIN FETCH p.dinhMuc JOIN FETCH p.matHangThanhPham "
            + "JOIN FETCH p.kho JOIN FETCH p.nhanVienThucHien WHERE p.trangThai = :trangThai "
            + "ORDER BY p.createdAt DESC")
    List<PhieuDongPallet> timTheoTrangThai(@Param("trangThai") String trangThai);
}