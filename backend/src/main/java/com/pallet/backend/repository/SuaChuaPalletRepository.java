package com.pallet.backend.repository;

import com.pallet.backend.entity.SuaChuaPallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuaChuaPalletRepository extends JpaRepository<SuaChuaPallet, Long> {

    List<SuaChuaPallet> findByMatHang_Id(Long matHangId);
    List<SuaChuaPallet> findByKho_Id(Long khoId);
    List<SuaChuaPallet> findByNhanVienThucHien_Id(Long nhanVienThucHienId);
    List<SuaChuaPallet> findByHoaDon_Id(Long hoaDonId);
}