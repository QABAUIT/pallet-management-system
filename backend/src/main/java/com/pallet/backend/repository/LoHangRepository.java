package com.pallet.backend.repository;

import com.pallet.backend.entity.LoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoHangRepository extends JpaRepository<LoHang, Long> {

    Optional<LoHang> findByMaLo(String maLo);
    List<LoHang> findByTrangThai(String trangThai);
    List<LoHang> findByMatHang_Id(Long matHangId);
    List<LoHang> findByKho_Id(Long khoId);
    List<LoHang> findByNcc_Id(Long nccId);
}