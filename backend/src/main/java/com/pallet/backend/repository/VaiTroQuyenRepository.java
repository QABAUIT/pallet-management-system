package com.pallet.backend.repository;

import com.pallet.backend.entity.VaiTroQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VaiTroQuyenRepository extends JpaRepository<VaiTroQuyen, Long>, JpaSpecificationExecutor<VaiTroQuyen> {

    List<VaiTroQuyen> findByVaiTroId(Long vaiTroId);

    Optional<VaiTroQuyen> findByVaiTroIdAndModuleCode(Long vaiTroId, String moduleCode);

    void deleteByVaiTroId(Long vaiTroId);
}
