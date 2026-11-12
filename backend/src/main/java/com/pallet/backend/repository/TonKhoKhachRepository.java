package com.pallet.backend.repository;

import com.pallet.backend.entity.TonKhoKhach;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TonKhoKhachRepository extends JpaRepository<TonKhoKhach, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TonKhoKhach t WHERE t.khachHang.id = :khachId AND t.matHang.id = :matHangId "
            + "AND t.kho.id = :khoId")
    Optional<TonKhoKhach> findForUpdate(@Param("khachId") Long khachId,
                                        @Param("matHangId") Long matHangId,
                                        @Param("khoId") Long khoId);

    @Query("SELECT t FROM TonKhoKhach t JOIN FETCH t.khachHang JOIN FETCH t.matHang JOIN FETCH t.kho "
            + "WHERE t.soLuong > 0 ORDER BY t.khachHang.id, t.kho.id, t.matHang.id")
    List<TonKhoKhach> timTatCaCoTon();

    @Query("SELECT t FROM TonKhoKhach t JOIN FETCH t.khachHang JOIN FETCH t.matHang JOIN FETCH t.kho "
            + "WHERE t.soLuong > 0 AND t.khachHang.id = :khachId ORDER BY t.kho.id, t.matHang.id")
    List<TonKhoKhach> timTheoKhach(@Param("khachId") Long khachId);
}