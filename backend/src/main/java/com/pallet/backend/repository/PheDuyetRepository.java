package com.pallet.backend.repository;

import com.pallet.backend.entity.PheDuyet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PheDuyetRepository extends JpaRepository<PheDuyet, Long>, JpaSpecificationExecutor<PheDuyet> {

    List<PheDuyet> findByTrangThai(String trangThai);

    List<PheDuyet> findByNguoiYeuCauId(Long nguoiYeuCauId);

    List<PheDuyet> findByDoiTuongLoaiAndDoiTuongId(String doiTuongLoai, Long doiTuongId);
}
