package com.pallet.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HoaDonChiTietResponse {
    private Long id;
    private String maHoaDon;
    private String loaiGiaoDich;
    private boolean laThu;
    private String trangThai;
    private boolean quaHan;
    private boolean daChotSo;
    private LocalDateTime ngayLap;
    private LocalDate hanThanhToan;
    private LocalDateTime ngayThanhToan;
    private String phuongThucThanhToan;
    private String ghiChu;
    private String tenKho;

    private BigDecimal tongTienHang;
    private BigDecimal tongTienChietKhau;
    private BigDecimal tyLeGiamGiaTong;
    private BigDecimal tienGiamGiaTong;
    private BigDecimal phiGiaoHang;
    private BigDecimal tienTruocThue;
    private BigDecimal tyLeVat;
    private BigDecimal tienVat;
    private BigDecimal tongThanhToan;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThoiLai;

    private DoiTac doiTac;
    private NhanVienLap nhanVienLap;
    private List<Dong> chiTiet;
    private List<LichSu> lichSu;

    @Data
    @Builder
    public static class DoiTac {
        private String loai; // khach_hang | nha_cung_cap
        private String ma;
        private String ten;
        private String mst;
        private String nguoiDaiDien;
        private String sdt;
        private String diaChi;
    }

    @Data
    @Builder
    public static class NhanVienLap {
        private Long id;
        private String maNv;
        private String hoTen;
        private String chucVu;
        private String tenBoPhan;
    }

    @Data
    @Builder
    public static class Dong {
        private Integer stt;
        private String maMatHang;
        private String tenMatHang;
        private String moTa;
        private String kichThuoc;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal tyLeChietKhau;
        private BigDecimal tienChietKhau;
        private BigDecimal thanhTien;
    }

    @Data
    @Builder
    public static class LichSu {
        private LocalDateTime thoiGian;
        private String hanhDong;
        private String nguoiSua;
        private String truongThayDoi;
        private String giaTriCu;
        private String giaTriMoi;
        private String lyDo;
    }
}