package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietDongPallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDongPalletRepository extends JpaRepository<ChiTietDongPallet, Long> {

    @Query("SELECT c FROM ChiTietDongPallet c JOIN FETCH c.matHang WHERE c.phieuDongPallet.id = :phieuId ORDER BY c.id")
    List<ChiTietDongPallet> timTheoPhieu(@Param("phieuId") Long phieuId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ChiTietDongPallet c WHERE c.phieuDongPallet.id = :phieuId")
    void xoaTheoPhieu(@Param("phieuId") Long phieuId);
}