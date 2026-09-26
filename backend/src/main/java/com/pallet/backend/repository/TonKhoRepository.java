package com.pallet.backend.repository;

import com.pallet.backend.entity.TonKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TonKhoRepository extends JpaRepository<TonKho, Long>, JpaSpecificationExecutor<TonKho> {

    Optional<TonKho> findByMatHangIdAndKhoId(Long matHangId, Long khoId);

    List<TonKho> findByKhoId(Long khoId);

    List<TonKho> findByMatHangId(Long matHangId);

    /**
     * Danh sách mặt hàng có tồn khả dụng (tồn - giữ chỗ) thấp hơn mức tối thiểu,
     * dùng để cảnh báo cần nhập thêm hàng.
     */
    @Query("SELECT t FROM TonKho t WHERE (t.soLuongTonKho - t.soLuongDaGiuCho) < t.soLuongToiThieu")
    List<TonKho> timTonKhoDuoiMucToiThieu();
}
