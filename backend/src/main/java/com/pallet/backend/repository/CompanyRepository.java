package com.pallet.backend.repository;

import com.pallet.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Bảng company là singleton (chỉ 1 dòng, id = 1 - xem CHECK trong schema).
 * Không cần JpaSpecificationExecutor vì không có nhu cầu tìm kiếm động.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    default Optional<Company> timThongTinCongTy() {
        return findById(1L);
    }
}
