package com.pallet.backend.repository;

import com.pallet.backend.entity.NhatKyHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhatKyHeThongRepository extends JpaRepository<NhatKyHeThong, Long> {

    List<NhatKyHeThong> findByNhanVien_Id(Long nhanVienId);
}