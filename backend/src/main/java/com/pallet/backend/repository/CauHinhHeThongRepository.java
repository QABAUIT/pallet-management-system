package com.pallet.backend.repository;

import com.pallet.backend.entity.CauHinhHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CauHinhHeThongRepository extends JpaRepository<CauHinhHeThong, Long> {

    Optional<CauHinhHeThong> findByKhoa(String khoa);
    List<CauHinhHeThong> findByNguoiCapNhat_Id(Long nguoiCapNhatId);
}