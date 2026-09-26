package com.pallet.backend.repository;

import com.pallet.backend.entity.NhaCungCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Long>, JpaSpecificationExecutor<NhaCungCap> {

    Optional<NhaCungCap> findByMaNcc(String maNcc);

    boolean existsByMaNcc(String maNcc);

    List<NhaCungCap> findByTrangThai(String trangThai);
}
