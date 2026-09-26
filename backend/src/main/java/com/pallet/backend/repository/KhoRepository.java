package com.pallet.backend.repository;

import com.pallet.backend.entity.Kho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface KhoRepository extends JpaRepository<Kho, Long>, JpaSpecificationExecutor<Kho> {

    Optional<Kho> findByMaKho(String maKho);

    boolean existsByMaKho(String maKho);

    List<Kho> findByTrangThai(String trangThai);

    List<Kho> findByLoaiDiaDiem(String loaiDiaDiem);
}
