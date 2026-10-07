package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietPhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietPhieuKhoRepository extends JpaRepository<ChiTietPhieuKho, Long> {

    List<ChiTietPhieuKho> findByPhieuKho_Id(Long phieuKhoId);
    List<ChiTietPhieuKho> findByMatHang_Id(Long matHangId);
    List<ChiTietPhieuKho> findByLo_Id(Long loId);

   // Tính tổng số lượng theo loại phiếu và trạng thái (Dùng cho Hàng đang chờ xử lý)
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai")
    Integer sumSoLuongByLoaiPhieuAndTrangThai(@org.springframework.data.repository.query.Param("loaiPhieu") String loaiPhieu, @org.springframework.data.repository.query.Param("trangThai") String trangThai);

    // Tính tổng số lượng hoàn thành TRONG MỘT KHOẢNG THỜI GIAN (Đã sửa lỗi ép kiểu Date)
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai AND c.phieuKho.ngayGio >= :tuNgay AND c.phieuKho.ngayGio <= :denNgay")
    Integer sumSoLuongByLoaiPhieuAndTrangThaiAndDateRange(
        @org.springframework.data.repository.query.Param("loaiPhieu") String loaiPhieu, 
        @org.springframework.data.repository.query.Param("trangThai") String trangThai, 
        @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay, 
        @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay
    );

    // Tính tổng xuất bảo dưỡng dựa vào từ khóa trong ghi chú
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.ghiChu LIKE %:keyword%")
    Integer sumSoLuongByLoaiPhieuAndGhiChuContains(@org.springframework.data.repository.query.Param("loaiPhieu") String loaiPhieu, @org.springframework.data.repository.query.Param("keyword") String keyword);
}