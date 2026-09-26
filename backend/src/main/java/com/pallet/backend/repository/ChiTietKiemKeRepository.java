package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietKiemKe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChiTietKiemKeRepository extends JpaRepository<ChiTietKiemKe, Long>, JpaSpecificationExecutor<ChiTietKiemKe> {

    List<ChiTietKiemKe> findByPhienKiemKeId(Long phienKiemKeId);

    List<ChiTietKiemKe> findByPhienKiemKeIdAndTrangThaiKiemKe(Long phienKiemKeId, String trangThaiKiemKe);
}
