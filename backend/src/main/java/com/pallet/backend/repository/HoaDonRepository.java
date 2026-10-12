package com.pallet.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pallet.backend.entity.HoaDon;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    List<HoaDon> findByTrangThai(String trangThai);
    List<HoaDon> findByKhachHang_Id(Long khachHangId);
    List<HoaDon> findByNcc_Id(Long nccId);
    List<HoaDon> findByKho_Id(Long khoId);
    List<HoaDon> findByNhanVienLap_Id(Long nhanVienLapId);

    @Query("SELECT h FROM HoaDon h LEFT JOIN FETCH h.khachHang LEFT JOIN FETCH h.ncc "
            + "LEFT JOIN FETCH h.nhanVienLap ORDER BY h.ngayLap DESC")
    List<HoaDon> timTatCa();

    @Query("SELECT h FROM HoaDon h LEFT JOIN FETCH h.khachHang LEFT JOIN FETCH h.ncc "
            + "LEFT JOIN FETCH h.nhanVienLap WHERE h.trangThai = :trangThai ORDER BY h.ngayLap DESC")
    List<HoaDon> timTheoTrangThai(@Param("trangThai") String trangThai);

    /** Gọi hàm fn_lay_ma_chung_tu trong V1 (an toàn đồng thời). Phải gọi trong @Transactional (không readOnly). */
    @Query(value = "SELECT fn_lay_ma_chung_tu(:loai, :tienTo)", nativeQuery = true)
    String layMaChungTu(@Param("loai") String loai, @Param("tienTo") String tienTo);
}