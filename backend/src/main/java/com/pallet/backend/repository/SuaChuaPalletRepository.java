package com.pallet.backend.repository;

import com.pallet.backend.entity.SuaChuaPallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SuaChuaPalletRepository extends JpaRepository<SuaChuaPallet, Long>, JpaSpecificationExecutor<SuaChuaPallet> {

    List<SuaChuaPallet> findByKhoId(Long khoId);

    List<SuaChuaPallet> findByMatHangId(Long matHangId);

    List<SuaChuaPallet> findByHoaDonId(Long hoaDonId);
}
