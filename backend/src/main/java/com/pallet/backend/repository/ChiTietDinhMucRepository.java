package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietDinhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDinhMucRepository extends JpaRepository<ChiTietDinhMuc, Long> {

    @Query("SELECT c FROM ChiTietDinhMuc c JOIN FETCH c.matHang WHERE c.dinhMuc.id = :dinhMucId ORDER BY c.id")
    List<ChiTietDinhMuc> timTheoDinhMuc(@Param("dinhMucId") Long dinhMucId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ChiTietDinhMuc c WHERE c.dinhMuc.id = :dinhMucId")
    void xoaTheoDinhMuc(@Param("dinhMucId") Long dinhMucId);
}