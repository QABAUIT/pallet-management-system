package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiPhiVanHanh;
import com.pallet.backend.entity.ChiTietHoaDon;
import com.pallet.backend.entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** Các truy vấn thống kê cho module Tài chính (chỉ đọc). Khoảng ngày: [tu, den). */
public interface TaiChinhRepository extends Repository<HoaDon, Long> {

    // ---------- Hóa đơn thu / chi đã thanh toán (theo NGÀY THANH TOÁN) ----------
    @Query("SELECT SUM(h.tongThanhToan) FROM HoaDon h WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'thu%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den")
    BigDecimal tongDoanhThu(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'thu%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den")
    Long demHoaDonThu(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    @Query("SELECT SUM(h.tongThanhToan) FROM HoaDon h WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'chi%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den")
    BigDecimal tongChiHoaDon(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    @Query("SELECT h.ngayThanhToan, h.tongThanhToan FROM HoaDon h WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'thu%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den")
    List<Object[]> doanhThuTheoNgay(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    @Query("SELECT h.ngayThanhToan, h.tongThanhToan FROM HoaDon h WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'chi%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den")
    List<Object[]> chiHoaDonTheoNgay(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    // ---------- Chi phí vận hành (theo NGÀY CHI), khoảng đóng [tu, den] ----------
    @Query("SELECT SUM(c.soTien) FROM ChiPhiVanHanh c WHERE c.ngayChi >= :tu AND c.ngayChi <= :den")
    BigDecimal tongChiVanHanh(@Param("tu") LocalDate tu, @Param("den") LocalDate den);

    @Query("SELECT c.loaiChiPhi, SUM(c.soTien) FROM ChiPhiVanHanh c "
            + "WHERE c.ngayChi >= :tu AND c.ngayChi <= :den GROUP BY c.loaiChiPhi")
    List<Object[]> chiVanHanhTheoLoai(@Param("tu") LocalDate tu, @Param("den") LocalDate den);

    @Query("SELECT c.ngayChi, c.soTien FROM ChiPhiVanHanh c WHERE c.ngayChi >= :tu AND c.ngayChi <= :den")
    List<Object[]> chiVanHanhTheoNgay(@Param("tu") LocalDate tu, @Param("den") LocalDate den);

    @Query("SELECT c FROM ChiPhiVanHanh c JOIN FETCH c.kho LEFT JOIN FETCH c.nguoiTao "
            + "WHERE c.ngayChi >= :tu AND c.ngayChi <= :den ORDER BY c.ngayChi DESC, c.id DESC")
    List<ChiPhiVanHanh> chiPhiTrongKy(@Param("tu") LocalDate tu, @Param("den") LocalDate den);

    // ---------- Cơ cấu pallet ----------
    @Query("SELECT m.tenMatHang, SUM(c.thanhTien), SUM(c.soLuong) FROM ChiTietHoaDon c "
            + "JOIN c.hoaDon h JOIN c.matHang m WHERE h.trangThai = 'da_thanh_toan' "
            + "AND h.loaiGiaoDich LIKE 'thu%' AND h.ngayThanhToan >= :tu AND h.ngayThanhToan < :den "
            + "GROUP BY m.id, m.tenMatHang ORDER BY SUM(c.thanhTien) DESC")
    List<Object[]> coCauMatHang(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    // ---------- Giao dịch gần đây & thuế (theo NGÀY LẬP) ----------
    @Query("SELECT h FROM HoaDon h JOIN FETCH h.khachHang WHERE h.loaiGiaoDich LIKE 'thu%' "
            + "AND h.trangThai IN ('cho_thanh_toan', 'da_thanh_toan') "
            + "AND h.ngayLap >= :tu AND h.ngayLap < :den ORDER BY h.ngayLap DESC")
    List<HoaDon> giaoDichGanDay(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den, Pageable pageable);

    @Query("SELECT c FROM ChiTietHoaDon c JOIN FETCH c.matHang WHERE c.hoaDon.id IN :ids ORDER BY c.id")
    List<ChiTietHoaDon> chiTietTheoHoaDon(@Param("ids") Collection<Long> ids);

    @Query("SELECT h FROM HoaDon h JOIN FETCH h.khachHang WHERE h.loaiGiaoDich LIKE 'thu%' "
            + "AND h.trangThai NOT IN ('da_huy', 'nhap') "
            + "AND h.ngayLap >= :tu AND h.ngayLap < :den ORDER BY h.ngayLap DESC")
    List<HoaDon> hoaDonThueTheoKy(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);

    // ---------- Tồn kho & khách hàng ----------
    @Query("SELECT SUM(t.soLuongTonKho) FROM TonKho t JOIN t.matHang m WHERE m.loaiMatHang = 'pallet'")
    Long tongTonKhoPallet();

    @Query("SELECT SUM(t.soLuongDaGiuCho) FROM TonKho t JOIN t.matHang m WHERE m.loaiMatHang = 'pallet'")
    Long tongGiuChoPallet();

    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.trangThai = 'dang_hop_tac'")
    Long demKhachHangHopTac();

    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.trangThai = 'dang_hop_tac' "
            + "AND k.createdAt >= :tu AND k.createdAt < :den")
    Long demKhachHangMoi(@Param("tu") LocalDateTime tu, @Param("den") LocalDateTime den);
}