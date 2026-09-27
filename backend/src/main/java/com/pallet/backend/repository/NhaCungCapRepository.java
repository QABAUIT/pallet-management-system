package com.pallet.backend.repository;

import com.pallet.backend.entity.NhaCungCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Long> {

    Optional<NhaCungCap> findByMaNcc(String maNcc);
    List<NhaCungCap> findByTrangThai(String trangThai);
    List<NhaCungCap> findByCreatedBy_Id(Long createdById);
}