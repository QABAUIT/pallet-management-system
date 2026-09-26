package com.pallet.backend.repository;

import com.pallet.backend.entity.BoDemChungTu;
import com.pallet.backend.entity.BoDemChungTuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository CHỈ khai báo hàm gọi fn_lay_ma_chung_tu(); việc gọi hàm này
 * (và ghép vào logic tạo hóa đơn/phiếu kho/báo giá...) thực hiện ở Service.
 *
 * fn_lay_ma_chung_tu() dùng INSERT ... ON CONFLICT DO UPDATE ... RETURNING
 * ở tầng DB nên atomic/an toàn khi nhiều giao dịch chạy song song - Service
 * chỉ cần gọi trong cùng transaction với thao tác tạo chứng từ, KHÔNG cần
 * tự khóa (lock) thêm gì cả.
 *
 * Ví dụ dùng trong Service:
 *   String maHoaDon = boDemChungTuRepository.layMaChungTu("hoa_don", "HD");
 */
public interface BoDemChungTuRepository extends JpaRepository<BoDemChungTu, BoDemChungTuId> {

    @Query(value = "SELECT fn_lay_ma_chung_tu(:loaiChungTu, :tienTo)", nativeQuery = true)
    String layMaChungTu(@Param("loaiChungTu") String loaiChungTu, @Param("tienTo") String tienTo);
}
