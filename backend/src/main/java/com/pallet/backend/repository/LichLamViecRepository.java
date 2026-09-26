package com.pallet.backend.repository;

import com.pallet.backend.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LichLamViecRepository extends JpaRepository<LichLamViec, Long>, JpaSpecificationExecutor<LichLamViec> {

    Optional<LichLamViec> findByNhanVienIdAndNgay(Long nhanVienId, LocalDate ngay);

    List<LichLamViec> findByNhanVienIdAndNgayBetween(Long nhanVienId, LocalDate tuNgay, LocalDate denNgay);

    List<LichLamViec> findByKhoIdAndNgay(Long khoId, LocalDate ngay);
}
