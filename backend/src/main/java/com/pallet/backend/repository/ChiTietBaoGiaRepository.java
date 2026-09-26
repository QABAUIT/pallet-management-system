package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietBaoGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChiTietBaoGiaRepository extends JpaRepository<ChiTietBaoGia, Long>, JpaSpecificationExecutor<ChiTietBaoGia> {

    List<ChiTietBaoGia> findByBaoGiaId(Long baoGiaId);

    void deleteByBaoGiaId(Long baoGiaId);
}
