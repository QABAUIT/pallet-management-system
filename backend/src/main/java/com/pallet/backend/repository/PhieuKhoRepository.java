package com.pallet.backend.repository;

import com.pallet.backend.entity.PhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuKhoRepository extends JpaRepository<PhieuKho, Long> {

    Optional<PhieuKho> findByMaPhieu(String maPhieu);
    List<PhieuKho> findByTrangThai(String trangThai);
    List<PhieuKho> findByKho_Id(Long khoId);
    List<PhieuKho> findByKhoDoiUng_Id(Long khoDoiUngId);
    List<PhieuKho> findByViTri_Id(Long viTriId);
    List<PhieuKho> findByHoaDon_Id(Long hoaDonId);
    List<PhieuKho> findByNhanVienGiaoNhan_Id(Long nhanVienGiaoNhanId);
    List<PhieuKho> findByNguoiDuyet_Id(Long nguoiDuyetId);
    List<PhieuKho> findByPhuongTien_Id(Long phuongTienId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT fn_lay_ma_chung_tu(:loaiChungTu, :tienTo)", nativeQuery = true)
    String getMaChungTu(@org.springframework.data.repository.query.Param("loaiChungTu") String loaiChungTu, @org.springframework.data.repository.query.Param("tienTo") String tienTo);
}