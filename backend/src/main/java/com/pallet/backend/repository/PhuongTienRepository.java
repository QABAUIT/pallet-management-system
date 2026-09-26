package com.pallet.backend.repository;

import com.pallet.backend.entity.PhuongTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PhuongTienRepository extends JpaRepository<PhuongTien, Long>, JpaSpecificationExecutor<PhuongTien> {

    Optional<PhuongTien> findByBienSo(String bienSo);

    boolean existsByBienSo(String bienSo);

    List<PhuongTien> findByTaiXeId(Long taiXeId);

    List<PhuongTien> findByTrangThai(String trangThai);
}
