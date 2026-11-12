package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Quy tắc báo giá (hàm thuần). */
public final class QuyTacBaoGia {

    private static final BigDecimal TRAM = BigDecimal.valueOf(100);

    private static final Map<String, Set<String>> CHUYEN_HOP_LE = Map.of(
            "nhap", Set.of("da_gui"),
            "da_gui", Set.of("da_chap_nhan", "tu_choi"));

    private QuyTacBaoGia() {
    }

    public record Dong(int soLuong, BigDecimal donGia, BigDecimal tyLeChietKhau) {}

    public record Tong(BigDecimal tongHang, BigDecimal tongChietKhau, BigDecimal tienGiam,
                       BigDecimal truocThue, BigDecimal tienVat, BigDecimal tongThanhToan) {}

    public static BigDecimal tienChietKhauDong(Dong d) {
        return d.donGia().multiply(BigDecimal.valueOf(d.soLuong()))
                .multiply(d.tyLeChietKhau()).divide(TRAM, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal thanhTienDong(Dong d) {
        return d.donGia().multiply(BigDecimal.valueOf(d.soLuong())).subtract(tienChietKhauDong(d));
    }

    /** Tổng hàng sau chiết khấu dòng, trừ giảm tổng đơn, cộng VAT. Không có phí giao hàng trong báo giá. */
    public static Tong tinhTong(List<Dong> dong, BigDecimal tyGiamTong, BigDecimal tyVat) {
        BigDecimal tongHang = BigDecimal.ZERO;
        BigDecimal tongCk = BigDecimal.ZERO;
        for (Dong d : dong) {
            tongHang = tongHang.add(thanhTienDong(d));
            tongCk = tongCk.add(tienChietKhauDong(d));
        }
        BigDecimal tienGiam = tongHang.multiply(tyGiamTong).divide(TRAM, 2, RoundingMode.HALF_UP);
        BigDecimal truocThue = tongHang.subtract(tienGiam);
        BigDecimal vat = truocThue.multiply(tyVat).divide(TRAM, 2, RoundingMode.HALF_UP);
        return new Tong(tongHang, tongCk, tienGiam, truocThue, vat, truocThue.add(vat));
    }

    public static void kiemTraChuyenTrangThai(String tu, String den) {
        Set<String> hopLe = CHUYEN_HOP_LE.get(tu);
        if (hopLe == null || !hopLe.contains(den)) {
            throw new BusinessException("Không thể chuyển báo giá từ trạng thái \"" + tu + "\" sang \"" + den + "\"");
        }
    }

    public static void kiemTraSoNgayHieuLuc(int soNgay) {
        if (soNgay < 1 || soNgay > 90) {
            throw new BusinessException("Số ngày hiệu lực của báo giá phải từ 1 đến 90");
        }
    }
}