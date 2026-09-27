package com.pallet.backend.repository;

import com.pallet.backend.entity.VaiTroQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaiTroQuyenRepository extends JpaRepository<VaiTroQuyen, Long> {

    List<VaiTroQuyen> findByVaiTro_Id(Long vaiTroId);
}