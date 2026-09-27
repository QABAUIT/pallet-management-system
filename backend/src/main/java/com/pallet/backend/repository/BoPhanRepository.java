package com.pallet.backend.repository;

import com.pallet.backend.entity.BoPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoPhanRepository extends JpaRepository<BoPhan, Long> {

    Optional<BoPhan> findByTenBoPhan(String tenBoPhan);
}