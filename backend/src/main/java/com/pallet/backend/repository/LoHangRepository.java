package com.pallet.backend.repository;

import com.pallet.backend.entity.LoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface LoHangRepository extends JpaRepository<LoHang, Long>, JpaSpecificationExecutor<LoHang> {

    Optional<LoHang> findByMaLo(String maLo);

    boolean existsByMaLo(String maLo);

    List<LoHang> findByMatHangIdAndKhoId(Long matHangId, Long khoId);

    /**
     * Lấy các lô còn hàng của 1 mặt hàng tại 1 kho, sắp theo ngày nhập tăng dần
     * (phục vụ xuất kho theo nguyên tắc FIFO - nhập trước xuất trước).
     */
    List<LoHang> findByMatHangIdAndKhoIdAndTrangThaiOrderByNgayNhapAsc(
            Long matHangId, Long khoId, String trangThai);

    List<LoHang> findByNccId(Long nccId);
}
