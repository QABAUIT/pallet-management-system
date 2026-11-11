package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuKho;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuKhoRepository extends JpaRepository<PhieuKho, Long>, JpaSpecificationExecutor<PhieuKho> {

    Optional<PhieuKho> findByMaPhieu(String maPhieu);
    List<PhieuKho> findByTrangThai(String trangThai);
    List<PhieuKho> findByKho_Id(Long khoId);
    List<PhieuKho> findByKhoDoiUng_Id(Long khoDoiUngId);
    List<PhieuKho> findByViTri_Id(Long viTriId);
    List<PhieuKho> findByHoaDon_Id(Long hoaDonId);
    List<PhieuKho> findByDonMua_Id(Long donMuaId);
    List<PhieuKho> findByPhieuTaiChe_Id(Long phieuTaiCheId);
    List<PhieuKho> findByNhanVienGiaoNhan_Id(Long nhanVienGiaoNhanId);
    List<PhieuKho> findByNguoiDuyet_Id(Long nguoiDuyetId);
    List<PhieuKho> findByPhuongTien_Id(Long phuongTienId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuKho p WHERE p.id = :id")
    Optional<PhieuKho> findForUpdate(@Param("id") Long id);

    @Query(value = "SELECT fn_lay_ma_chung_tu(:loaiChungTu, :tienTo)", nativeQuery = true)
    String getMaChungTu(@Param("loaiChungTu") String loaiChungTu, @Param("tienTo") String tienTo);
}
