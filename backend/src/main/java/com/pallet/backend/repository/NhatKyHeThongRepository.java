package com.pallet.backend.repository;

import com.pallet.backend.entity.NhatKyHeThong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NhatKyHeThongRepository extends JpaRepository<NhatKyHeThong, Long>, JpaSpecificationExecutor<NhatKyHeThong> {

    Page<NhatKyHeThong> findByModule(String module, Pageable pageable);

    List<NhatKyHeThong> findByNhanVienId(Long nhanVienId);

    List<NhatKyHeThong> findByDoiTuongLoaiAndDoiTuongId(String doiTuongLoai, Long doiTuongId);
}
