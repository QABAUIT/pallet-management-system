package com.pallet.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pallet.backend.entity.PhieuTaiChe;

import jakarta.persistence.LockModeType;

@Repository
public interface PhieuTaiCheRepository extends JpaRepository<PhieuTaiChe, Long> {

    Optional<PhieuTaiChe> findByMaPhieu(String maPhieu);

    boolean existsByMaPhieu(String maPhieu);

    List<PhieuTaiChe> findByKhoId(Long khoId);

    List<PhieuTaiChe> findByLoHangId(Long loHangId);

    List<PhieuTaiChe> findByTrangThai(String trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuTaiChe p WHERE p.id = :id")
    Optional<PhieuTaiChe> findForUpdate(@Param("id") Long id);

    /** Tổng pallet của các phiếu NHÁP trên 1 lô (trừ phiếu boQuaId; truyền -1 nếu không loại phiếu nào). */
    @Query("SELECT COALESCE(SUM(p.soLuongDaXuLy), 0) FROM PhieuTaiChe p "
            + "WHERE p.loHang.id = :loId AND p.trangThai = 'nhap' AND p.id <> :boQuaId")
    Long tongDangCho(@Param("loId") Long loId, @Param("boQuaId") Long boQuaId);

    @Query("SELECT p FROM PhieuTaiChe p JOIN FETCH p.loHang l JOIN FETCH l.matHang JOIN FETCH p.kho "
            + "JOIN FETCH p.nhanVienThucHien ORDER BY p.createdAt DESC")
    List<PhieuTaiChe> timTatCa();

    @Query("SELECT p FROM PhieuTaiChe p JOIN FETCH p.loHang l JOIN FETCH l.matHang JOIN FETCH p.kho "
            + "JOIN FETCH p.nhanVienThucHien WHERE p.trangThai = :trangThai ORDER BY p.createdAt DESC")
    List<PhieuTaiChe> timTheoTrangThai(@Param("trangThai") String trangThai);
}