package com.pallet.backend.repository;

import com.pallet.backend.entity.BoDemChungTu;
import com.pallet.backend.entity.BoDemChungTuId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoDemChungTuRepository extends JpaRepository<BoDemChungTu, BoDemChungTuId> {

    /**
     * Khóa bi quan (SELECT ... FOR UPDATE) khi đọc bộ đếm để tăng số tuần tự,
     * bắt buộc dùng trong 1 @Transactional khi sinh mã chứng từ mới.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BoDemChungTu b WHERE b.id.loaiChungTu = :loaiChungTu AND b.id.nam = :nam")
    Optional<BoDemChungTu> findForUpdate(@Param("loaiChungTu") String loaiChungTu, @Param("nam") Integer nam);
}
