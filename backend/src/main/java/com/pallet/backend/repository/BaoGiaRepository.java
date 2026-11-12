package com.pallet.backend.repository;

import com.pallet.backend.entity.BaoGia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaoGiaRepository extends JpaRepository<BaoGia, Long> {

    Optional<BaoGia> findByMaBaoGia(String maBaoGia);
    List<BaoGia> findByTrangThai(String trangThai);
    List<BaoGia> findByKhachHang_Id(Long khachHangId);
    List<BaoGia> findByNguoiTao_Id(Long nguoiTaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BaoGia b WHERE b.id = :id")
    Optional<BaoGia> findForUpdate(@Param("id") Long id);

    @Query("SELECT b FROM BaoGia b JOIN FETCH b.khachHang JOIN FETCH b.nguoiTao ORDER BY b.id DESC")
    List<BaoGia> timTatCa();

    @Query("SELECT b FROM BaoGia b JOIN FETCH b.khachHang JOIN FETCH b.nguoiTao "
            + "WHERE b.trangThai = :trangThai ORDER BY b.id DESC")
    List<BaoGia> timTheoTrangThai(@Param("trangThai") String trangThai);

    /** Báo giá nháp/đã gửi quá hạn hiệu lực chuyển sang het_han. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE BaoGia b SET b.trangThai = 'het_han' "
            + "WHERE b.trangThai IN ('nhap','da_gui') AND b.hanHieuLuc < CURRENT_DATE")
    int danhDauHetHan();
}