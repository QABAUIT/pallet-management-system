package com.pallet.backend.repository;

import com.pallet.backend.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long>, JpaSpecificationExecutor<CaLamViec> {

    List<CaLamViec> findAllByOrderByGioBatDauAsc();
}
