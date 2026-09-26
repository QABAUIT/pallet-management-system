package com.pallet.backend.repository;

import com.pallet.backend.entity.BangGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BangGiaRepository extends JpaRepository<BangGia, Long>, JpaSpecificationExecutor<BangGia> {

    List<BangGia> findByMatHangId(Long matHangId);

    List<BangGia> findByKhachHangId(Long khachHangId);

    /**
     * Tìm đơn giá đang hiệu lực tại 1 thời điểm cho 1 mặt hàng, ưu tiên bảng giá
     * riêng cho khách hàng (nếu có), sau đó tới bảng giá chung (khach_hang_id NULL).
     */
    @Query("""
            SELECT b FROM BangGia b
            WHERE b.matHang.id = :matHangId
              AND (b.khachHang.id = :khachHangId OR b.khachHang IS NULL)
              AND b.ngayBatDauHieuLuc <= :ngay
              AND (b.ngayKetThucHieuLuc IS NULL OR b.ngayKetThucHieuLuc >= :ngay)
            ORDER BY b.khachHang.id NULLS LAST
            """)
    List<BangGia> timDonGiaHieuLuc(@Param("matHangId") Long matHangId,
                                    @Param("khachHangId") Long khachHangId,
                                    @Param("ngay") LocalDate ngay);
}
