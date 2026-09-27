package com.pallet.backend.repository;

import com.pallet.backend.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long> {

}