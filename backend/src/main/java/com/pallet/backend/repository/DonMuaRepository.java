package com.pallet.backend.repository;

import com.pallet.backend.entity.DonMua;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonMuaRepository extends JpaRepository<DonMua, Long> {

    Optional<DonMua> findByMaDonMua(String maDonMua);
    List<DonMua> findByTrangThai(String trangThai);
    List<DonMua> findByNcc_Id(Long nccId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DonMua d WHERE d.id = :id")
    Optional<DonMua> findForUpdate(@Param("id") Long id);

    @Query("SELECT d FROM DonMua d JOIN FETCH d.ncc JOIN FETCH d.kho JOIN FETCH d.nhanVienLap "
            + "ORDER BY d.createdAt DESC")
    List<DonMua> timTatCa();

    @Query("SELECT d FROM DonMua d JOIN FETCH d.ncc JOIN FETCH d.kho JOIN FETCH d.nhanVienLap "
            + "WHERE d.trangThai = :trangThai ORDER BY d.createdAt DESC")
    List<DonMua> timTheoTrangThai(@Param("trangThai") String trangThai);

    /** Gọi fn_lay_ma_chung_tu (an toàn đồng thời). Phải gọi trong @Transactional không readOnly. */
    @Query(value = "SELECT fn_lay_ma_chung_tu(:loai, :tienTo)", nativeQuery = true)
    String layMaChungTu(@Param("loai") String loai, @Param("tienTo") String tienTo);
}