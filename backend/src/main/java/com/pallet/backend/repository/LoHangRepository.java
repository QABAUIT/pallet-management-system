package com.pallet.backend.repository;

import com.pallet.backend.entity.LoHang;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoHangRepository extends JpaRepository<LoHang, Long> {

    Optional<LoHang> findByMaLo(String maLo);
    List<LoHang> findByTrangThai(String trangThai);
    List<LoHang> findByMatHang_Id(Long matHangId);
    List<LoHang> findByKho_Id(Long khoId);
    List<LoHang> findByNcc_Id(Long nccId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LoHang l WHERE l.id = :id")
    Optional<LoHang> findForUpdate(@Param("id") Long id);

    /** Lô pallet đang chờ tái chế còn hàng (mọi kho). */
    @Query("SELECT l FROM LoHang l JOIN FETCH l.matHang JOIN FETCH l.kho LEFT JOIN FETCH l.ncc "
            + "WHERE l.mucDichNhap = 'cho_tai_che' AND l.trangThai = 'con_hang' AND l.soLuongConLai > 0 "
            + "ORDER BY l.ngayNhap, l.id")
    List<LoHang> timLoChoTaiChe();

    @Query("SELECT l FROM LoHang l JOIN FETCH l.matHang JOIN FETCH l.kho LEFT JOIN FETCH l.ncc "
            + "WHERE l.mucDichNhap = 'cho_tai_che' AND l.trangThai = 'con_hang' AND l.soLuongConLai > 0 "
            + "AND l.kho.id = :khoId ORDER BY l.ngayNhap, l.id")
    List<LoHang> timLoChoTaiCheTheoKho(@Param("khoId") Long khoId);
}