package com.pallet.backend.repository;

import com.pallet.backend.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    Optional<NhanVien> findByMaNv(String maNv);

    Optional<NhanVien> findByEmail(String email);

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    List<NhanVien> findByTrangThai(String trangThai);

    List<NhanVien> findByVaiTro_Id(Long vaiTroId);

    List<NhanVien> findByKho_Id(Long khoId);

    List<NhanVien> findByBoPhan_Id(Long boPhanId);
    
    boolean existsByEmail(String email);

    @Query(value = "SELECT nextval('ma_nv_seq')", nativeQuery = true)
    Long nextMaNvSeq();
}