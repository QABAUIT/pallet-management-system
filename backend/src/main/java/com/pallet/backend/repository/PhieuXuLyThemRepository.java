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

    /** Tổng số chiếc đang nằm trong các phiếu NHÁP khác của cùng kho + nhóm nguồn. */
    @Query("SELECT COALESCE(SUM(x.soDatLai + x.soPheLieu), 0) FROM PhieuXuLyThem x "
            + "WHERE x.kho.id = :khoId AND x.loaiNguon = :loaiNguon "
            + "AND x.trangThai = 'nhap' AND x.id <> :boQuaPhieuId")
    Long tongDangCho(@Param("khoId") Long khoId, @Param("loaiNguon") String loaiNguon,
                     @Param("boQuaPhieuId") Long boQuaPhieuId);
}