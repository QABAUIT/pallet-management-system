package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietKiemKe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietKiemKeRepository extends JpaRepository<ChiTietKiemKe, Long> {

    List<ChiTietKiemKe> findByPhienKiemKe_Id(Long phienKiemKeId);
    List<ChiTietKiemKe> findByMatHang_Id(Long matHangId);
    List<ChiTietKiemKe> findByViTri_Id(Long viTriId);
    List<ChiTietKiemKe> findByNguoiKiem_Id(Long nguoiKiemId);
}