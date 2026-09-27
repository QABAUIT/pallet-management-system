package com.pallet.backend.repository;

import com.pallet.backend.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Long> {

    List<LichLamViec> findByNhanVien_Id(Long nhanVienId);
    List<LichLamViec> findByCaLamViec_Id(Long caLamViecId);
    List<LichLamViec> findByKho_Id(Long khoId);
    List<LichLamViec> findByNguoiXepLich_Id(Long nguoiXepLichId);
}