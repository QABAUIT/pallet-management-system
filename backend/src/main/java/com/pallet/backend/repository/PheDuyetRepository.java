package com.pallet.backend.repository;

import com.pallet.backend.entity.PheDuyet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PheDuyetRepository extends JpaRepository<PheDuyet, Long> {

    List<PheDuyet> findByTrangThai(String trangThai);
    List<PheDuyet> findByNguoiYeuCau_Id(Long nguoiYeuCauId);
    List<PheDuyet> findByNguoiDuyet_Id(Long nguoiDuyetId);
}