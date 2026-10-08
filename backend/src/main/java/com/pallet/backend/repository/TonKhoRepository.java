package com.pallet.backend.repository;

import com.pallet.backend.entity.TonKho;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TonKhoRepository extends JpaRepository<TonKho, Long> {

    List<TonKho> findByMatHang_Id(Long matHangId);

    List<TonKho> findByKho_Id(Long khoId);

    List<TonKho> findByViTri_Id(Long viTriId);

    Optional<TonKho> findByMatHang_IdAndKho_Id(Long matHangId, Long khoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TonKho t
        WHERE t.matHang.id = :matHangId
          AND t.kho.id = :khoId
    """)
    Optional<TonKho> findForUpdate(
            @Param("matHangId") Long matHangId,
            @Param("khoId") Long khoId
    );

    @Query("SELECT COALESCE(SUM(t.soLuongTonKho), 0) FROM TonKho t WHERE t.matHang.id = :matHangId")
    Integer sumSoLuongTonKho(@Param("matHangId") Long matHangId);

    @Query("SELECT COALESCE(SUM(t.soLuongTonKho - t.soLuongDaGiuCho), 0) FROM TonKho t WHERE t.matHang.id = :matHangId")
    Integer sumSoLuongKhaDung(@Param("matHangId") Long matHangId);
}