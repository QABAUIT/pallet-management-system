package com.pallet.backend.repository;

import com.pallet.backend.entity.TokenDatLaiMatKhau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenDatLaiMatKhauRepository extends JpaRepository<TokenDatLaiMatKhau, Long> {

    Optional<TokenDatLaiMatKhau> findByToken(String token);
    List<TokenDatLaiMatKhau> findByNhanVien_Id(Long nhanVienId);
}