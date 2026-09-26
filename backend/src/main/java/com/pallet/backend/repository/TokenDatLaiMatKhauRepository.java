package com.pallet.backend.repository;

import com.pallet.backend.entity.TokenDatLaiMatKhau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenDatLaiMatKhauRepository extends JpaRepository<TokenDatLaiMatKhau, Long>, JpaSpecificationExecutor<TokenDatLaiMatKhau> {

    Optional<TokenDatLaiMatKhau> findByToken(String token);

    List<TokenDatLaiMatKhau> findByNhanVienIdAndDaSuDungFalse(Long nhanVienId);

    void deleteByThoiGianHetHanBefore(LocalDateTime moc);
}
