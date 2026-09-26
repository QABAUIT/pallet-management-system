package com.pallet.backend.repository;

import com.pallet.backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long>, JpaSpecificationExecutor<NhanVien> {

    Optional<NhanVien> findByMaNv(String maNv);

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);

    Optional<NhanVien> findByEmail(String email);

    boolean existsByMaNv(String maNv);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    List<NhanVien> findByKhoId(Long khoId);

    List<NhanVien> findByVaiTroId(Long vaiTroId);

    List<NhanVien> findByBoPhanId(Long boPhanId);

    List<NhanVien> findByTrangThai(String trangThai);
}
