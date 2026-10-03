package com.pallet.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pallet.backend.entity.ChiTietTaiChe;

@Repository
public interface ChiTietTaiCheRepository extends JpaRepository<ChiTietTaiChe, Long> {

    List<ChiTietTaiChe> findByPhieuTaiCheId(Long phieuTaiCheId);

    void deleteByPhieuTaiCheId(Long phieuTaiCheId);

}