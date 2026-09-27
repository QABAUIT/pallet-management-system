package com.pallet.backend.repository;

import com.pallet.backend.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {

    Optional<VaiTro> findByMaVaiTro(String maVaiTro);
}