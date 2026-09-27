package com.pallet.backend.repository;

import com.pallet.backend.entity.PhuongTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhuongTienRepository extends JpaRepository<PhuongTien, Long> {

    Optional<PhuongTien> findByBienSo(String bienSo);
    List<PhuongTien> findByTrangThai(String trangThai);
    List<PhuongTien> findByTaiXe_Id(Long taiXeId);
}