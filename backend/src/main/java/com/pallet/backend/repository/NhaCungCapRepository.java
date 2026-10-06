package com.pallet.backend.repository;

import com.pallet.backend.entity.NhaCungCap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhaCungCapRepository extends JpaRepository<NhaCungCap,Long> {

    Optional findByMaNcc(String maNcc);
    
    List findByTrangThai(String trangThai);
    
    List findByCreatedBy_Id(Long createdById);

@Query("""
    SELECT n FROM NhaCungCap n
    WHERE n.trangThai <> 'DELETED'
      AND (LOWER(n.maNcc)  LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(n.tenNcc) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(n.sdt)    LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:nhomHang = '' OR n.nhomHang = :nhomHang)
    """)
Page<NhaCungCap> searchAndFilter(
        @Param("keyword") String keyword,
        @Param("nhomHang") String nhomHang,
        Pageable pageable);



  @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(ma_ncc FROM 5) AS INTEGER)), 0) " +
               "FROM nha_cung_cap WHERE ma_ncc ~ '^NCC-[0-9]+$'", nativeQuery = true)
int findMaxMaNccNumber();
}
