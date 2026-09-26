package com.pallet.backend.repository;

import com.pallet.backend.entity.MucTieuDoanhThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MucTieuDoanhThuRepository extends JpaRepository<MucTieuDoanhThu, Long>, JpaSpecificationExecutor<MucTieuDoanhThu> {

    List<MucTieuDoanhThu> findByKhoId(Long khoId);

    Optional<MucTieuDoanhThu> findByKhoIdAndKyAndNamKyAndThangKy(
            Long khoId, String ky, Integer namKy, Short thangKy);

    List<MucTieuDoanhThu> findByKyAndNamKy(String ky, Integer namKy);
}
