package com.pallet.backend.repository;

import com.pallet.backend.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long>, JpaSpecificationExecutor<ThongBao> {

    Page<ThongBao> findByNguoiNhanIdOrderByThoiGianTaoDesc(Long nguoiNhanId, Pageable pageable);

    List<ThongBao> findByNguoiNhanIdAndDaDocFalse(Long nguoiNhanId);

    List<ThongBao> findByVaiTroNhanId(Long vaiTroNhanId);

    long countByNguoiNhanIdAndDaDocFalse(Long nguoiNhanId);

    @Modifying
    @Query("UPDATE ThongBao t SET t.daDoc = true WHERE t.id = :id")
    void danhDauDaDoc(Long id);
}
