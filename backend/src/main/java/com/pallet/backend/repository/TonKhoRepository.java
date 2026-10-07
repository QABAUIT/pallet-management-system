package com.pallet.backend.repository;

import com.pallet.backend.entity.TonKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TonKhoRepository extends JpaRepository<TonKho, Long>, JpaSpecificationExecutor<TonKho> { 

    List<TonKho> findByMatHang_Id(Long matHangId);
    List<TonKho> findByKho_Id(Long khoId);
    List<TonKho> findByViTri_Id(Long viTriId);

    Optional<TonKho> findByMatHang_IdAndKho_Id(Long matHangId, Long khoId);

    @Query("SELECT COALESCE(SUM(t.soLuongTonKho), 0) FROM TonKho t WHERE t.matHang.id = :matHangId")
    Integer sumSoLuongTonKho(@Param("matHangId") Long matHangId);

    @Query("SELECT COALESCE(SUM(t.soLuongTonKho - t.soLuongDaGiuCho), 0) FROM TonKho t WHERE t.matHang.id = :matHangId")
    Integer sumSoLuongKhaDung(@Param("matHangId") Long matHangId);

    // Đếm số vị trí đang chứa hàng (có Tồn kho > 0)
    @Query("SELECT COUNT(DISTINCT t.viTri.id) FROM TonKho t WHERE t.soLuongTonKho > 0 AND t.viTri IS NOT NULL")
    Integer countViTriDaSuDung();
}