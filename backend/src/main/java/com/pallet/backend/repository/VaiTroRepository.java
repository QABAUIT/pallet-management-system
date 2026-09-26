package com.pallet.backend.repository;

import com.pallet.backend.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Long>, JpaSpecificationExecutor<VaiTro> {

    Optional<VaiTro> findByMaVaiTro(String maVaiTro);

    boolean existsByMaVaiTro(String maVaiTro);
}
