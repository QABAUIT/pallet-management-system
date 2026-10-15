package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietPhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChiTietPhieuKhoRepository extends JpaRepository<ChiTietPhieuKho, Long> {

    List<ChiTietPhieuKho> findByPhieuKho_Id(Long phieuKhoId);
    List<ChiTietPhieuKho> findByMatHang_Id(Long matHangId);
    List<ChiTietPhieuKho> findByLo_Id(Long loId);

    // ===== Các query cũ (giữ nguyên) =====
    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai")
    Integer sumSoLuongByLoaiPhieuAndTrangThai(@Param("loaiPhieu") String loaiPhieu, @Param("trangThai") String trangThai);

    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai AND c.phieuKho.ngayGio >= :tuNgay AND c.phieuKho.ngayGio <= :denNgay")
    Integer sumSoLuongByLoaiPhieuAndTrangThaiAndDateRange(
            @Param("loaiPhieu") String loaiPhieu,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay);

    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c WHERE c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.ghiChu LIKE %:keyword%")
    Integer sumSoLuongByLoaiPhieuAndGhiChuContains(@Param("loaiPhieu") String loaiPhieu, @Param("keyword") String keyword);

    // ===== Thống kê THEO KHO cho các thẻ trên trang Quản lí kho =====

    /** Tổng số lượng của phiếu cùng loại + trạng thái trong 1 kho (không giới hạn thời gian). */
    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c "
            + "WHERE c.phieuKho.kho.id = :khoId AND c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai")
    Integer sumSoLuongTheoKho(@Param("khoId") Long khoId,
                              @Param("loaiPhieu") String loaiPhieu,
                              @Param("trangThai") String trangThai);

    /** Như trên nhưng chỉ tính phiếu có ngày giờ trong [tuNgay, denNgay). */
    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c "
            + "WHERE c.phieuKho.kho.id = :khoId AND c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai "
            + "AND c.phieuKho.ngayGio >= :tuNgay AND c.phieuKho.ngayGio < :denNgay")
    Integer sumSoLuongTheoKhoVaThoiGian(@Param("khoId") Long khoId,
                                        @Param("loaiPhieu") String loaiPhieu,
                                        @Param("trangThai") String trangThai,
                                        @Param("tuNgay") LocalDateTime tuNgay,
                                        @Param("denNgay") LocalDateTime denNgay);

    /** Giá trị = số lượng x đơn giá bán (mat_hang.don_gia_ban). Trả null nếu không có dòng nào. */
    @Query("SELECT SUM(c.soLuong * c.matHang.donGiaBan) FROM ChiTietPhieuKho c "
            + "WHERE c.phieuKho.kho.id = :khoId AND c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai "
            + "AND c.phieuKho.ngayGio >= :tuNgay AND c.phieuKho.ngayGio < :denNgay")
    BigDecimal sumGiaTriTheoKhoVaThoiGian(@Param("khoId") Long khoId,
                                          @Param("loaiPhieu") String loaiPhieu,
                                          @Param("trangThai") String trangThai,
                                          @Param("tuNgay") LocalDateTime tuNgay,
                                          @Param("denNgay") LocalDateTime denNgay);

    /** Như sumSoLuongTheoKhoVaThoiGian nhưng ghi chú phiếu phải khớp mẫu LIKE (vd "Loại xuất: Bảo trì%"). */
    @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietPhieuKho c "
            + "WHERE c.phieuKho.kho.id = :khoId AND c.phieuKho.loaiPhieu = :loaiPhieu AND c.phieuKho.trangThai = :trangThai "
            + "AND c.phieuKho.ngayGio >= :tuNgay AND c.phieuKho.ngayGio < :denNgay "
            + "AND c.phieuKho.ghiChu LIKE :mauGhiChu")
    Integer sumSoLuongTheoKhoThoiGianVaGhiChu(@Param("khoId") Long khoId,
                                              @Param("loaiPhieu") String loaiPhieu,
                                              @Param("trangThai") String trangThai,
                                              @Param("tuNgay") LocalDateTime tuNgay,
                                              @Param("denNgay") LocalDateTime denNgay,
                                              @Param("mauGhiChu") String mauGhiChu);
}