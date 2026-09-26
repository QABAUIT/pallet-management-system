package com.pallet.backend.repository;

import com.pallet.backend.entity.PhienKiemKe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PhienKiemKeRepository extends JpaRepository<PhienKiemKe, Long>, JpaSpecificationExecutor<PhienKiemKe> {

    Optional<PhienKiemKe> findByMaPhien(String maPhien);

    boolean existsByMaPhien(String maPhien);

    List<PhienKiemKe> findByKhoId(Long khoId);

    List<PhienKiemKe> findByTrangThai(String trangThai);
}
