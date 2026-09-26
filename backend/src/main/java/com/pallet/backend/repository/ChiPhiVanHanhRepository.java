package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiPhiVanHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ChiPhiVanHanhRepository extends JpaRepository<ChiPhiVanHanh, Long>, JpaSpecificationExecutor<ChiPhiVanHanh> {

    Optional<ChiPhiVanHanh> findByMaChiPhi(String maChiPhi);

    boolean existsByMaChiPhi(String maChiPhi);

    List<ChiPhiVanHanh> findByKhoId(Long khoId);

    List<ChiPhiVanHanh> findByLoaiChiPhi(String loaiChiPhi);
}
