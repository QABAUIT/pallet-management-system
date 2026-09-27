package com.pallet.backend.repository;

import com.pallet.backend.entity.MucTieuDoanhThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MucTieuDoanhThuRepository extends JpaRepository<MucTieuDoanhThu, Long> {

    List<MucTieuDoanhThu> findByKho_Id(Long khoId);
    List<MucTieuDoanhThu> findByNguoiTao_Id(Long nguoiTaoId);
}