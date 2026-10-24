package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuThaoDo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuThaoDoRepository extends JpaRepository<PhieuThaoDo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PhieuThaoDo t WHERE t.id = :id")
    Optional<PhieuThaoDo> findForUpdate(@Param("id") Long id);

    /** Tổng pallet các đợt theo trạng thái (trừ đợt boQuaId; truyền -1 nếu không loại đợt nào). */
    @Query("SELECT COALESCE(SUM(t.soPalletThaoDo), 0) FROM PhieuThaoDo t "
            + "WHERE t.phieuTaiChe.id = :phieuId AND t.trangThai = :trangThai AND t.id <> :boQuaId")
    Long tongTheoTrangThai(@Param("phieuId") Long phieuId,
                           @Param("trangThai") String trangThai,
                           @Param("boQuaId") Long boQuaId);

    @Query("SELECT t FROM PhieuThaoDo t JOIN FETCH t.phieuTaiChe p JOIN FETCH p.loHang "
            + "JOIN FETCH t.kho JOIN FETCH t.nhanVienThucHien ORDER BY t.createdAt DESC")
    List<PhieuThaoDo> timTatCa();

    @Query("SELECT t FROM PhieuThaoDo t JOIN FETCH t.phieuTaiChe p JOIN FETCH p.loHang "
            + "JOIN FETCH t.kho JOIN FETCH t.nhanVienThucHien "
            + "WHERE p.id = :phieuId ORDER BY t.createdAt DESC")
    List<PhieuThaoDo> timTheoPhieuTaiChe(@Param("phieuId") Long phieuId);
}