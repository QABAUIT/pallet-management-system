package com.pallet.backend.repository;

import com.pallet.backend.entity.CauHinhHeThong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CauHinhHeThongRepository extends JpaRepository<CauHinhHeThong, Long> {

    Optional<CauHinhHeThong> findByKhoa(String khoa);

    boolean existsByKhoa(String khoa);
}
