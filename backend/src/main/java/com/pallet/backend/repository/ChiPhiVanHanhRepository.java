package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiPhiVanHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiPhiVanHanhRepository extends JpaRepository<ChiPhiVanHanh, Long> {

    Optional<ChiPhiVanHanh> findByMaChiPhi(String maChiPhi);
    List<ChiPhiVanHanh> findByKho_Id(Long khoId);
    List<ChiPhiVanHanh> findByNguoiTao_Id(Long nguoiTaoId);
}