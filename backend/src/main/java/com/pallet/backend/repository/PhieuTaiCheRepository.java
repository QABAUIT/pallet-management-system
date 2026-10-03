package com.pallet.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pallet.backend.entity.PhieuTaiChe;

@Repository
public interface PhieuTaiCheRepository extends JpaRepository<PhieuTaiChe, Long> {

    Optional<PhieuTaiChe> findByMaPhieu(String maPhieu);

    boolean existsByMaPhieu(String maPhieu);

    List<PhieuTaiChe> findByKhoId(Long khoId);

    List<PhieuTaiChe> findByLoHangId(Long loHangId);

    List<PhieuTaiChe> findByTrangThai(String trangThai);

}