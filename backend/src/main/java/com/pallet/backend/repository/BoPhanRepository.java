package com.pallet.backend.repository;

import com.pallet.backend.entity.BoPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BoPhanRepository extends JpaRepository<BoPhan, Long>, JpaSpecificationExecutor<BoPhan> {

    Optional<BoPhan> findByTenBoPhan(String tenBoPhan);

    boolean existsByTenBoPhan(String tenBoPhan);
}
