package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuyTacBaoGiaTest {

    @Test
    void tinhTongKhongGiamTong() {
        var dong = new QuyTacBaoGia.Dong(10, new BigDecimal("90000"), new BigDecimal("5"));
        assertEquals(new BigDecimal("45000.00"), QuyTacBaoGia.tienChietKhauDong(dong));
        assertEquals(new BigDecimal("855000.00"), QuyTacBaoGia.thanhTienDong(dong));

        var tong = QuyTacBaoGia.tinhTong(List.of(dong), BigDecimal.ZERO, BigDecimal.valueOf(8));
        assertEquals(new BigDecimal("855000.00"), tong.tongHang());
        assertEquals(new BigDecimal("68400.00"), tong.tienVat());
        assertEquals(new BigDecimal("923400.00"), tong.tongThanhToan());
    }

    @Test
    void tinhTongCoGiamTong() {
        var d1 = new QuyTacBaoGia.Dong(100, new BigDecimal("150000"), BigDecimal.ZERO);
        var d2 = new QuyTacBaoGia.Dong(50, new BigDecimal("260000"), BigDecimal.ZERO);
        var tong = QuyTacBaoGia.tinhTong(List.of(d1, d2), BigDecimal.valueOf(10), BigDecimal.valueOf(8));
        assertEquals(new BigDecimal("28000000.00"), tong.tongHang());
        assertEquals(new BigDecimal("2800000.00"), tong.tienGiam());
        assertEquals(new BigDecimal("25200000.00"), tong.truocThue());
        assertEquals(new BigDecimal("27216000.00"), tong.tongThanhToan());
    }

    @Test
    void chuyenTrangThai() {
        assertDoesNotThrow(() -> QuyTacBaoGia.kiemTraChuyenTrangThai("nhap", "da_gui"));
        assertDoesNotThrow(() -> QuyTacBaoGia.kiemTraChuyenTrangThai("da_gui", "da_chap_nhan"));
        assertDoesNotThrow(() -> QuyTacBaoGia.kiemTraChuyenTrangThai("da_gui", "tu_choi"));
        assertThrows(BusinessException.class, () -> QuyTacBaoGia.kiemTraChuyenTrangThai("nhap", "da_chap_nhan"));
        assertThrows(BusinessException.class, () -> QuyTacBaoGia.kiemTraChuyenTrangThai("tu_choi", "da_gui"));
        assertThrows(BusinessException.class, () -> QuyTacBaoGia.kiemTraChuyenTrangThai("da_chap_nhan", "tu_choi"));
    }

    @Test
    void soNgayHieuLuc() {
        assertDoesNotThrow(() -> QuyTacBaoGia.kiemTraSoNgayHieuLuc(15));
        assertThrows(BusinessException.class, () -> QuyTacBaoGia.kiemTraSoNgayHieuLuc(0));
        assertThrows(BusinessException.class, () -> QuyTacBaoGia.kiemTraSoNgayHieuLuc(91));
    }
}