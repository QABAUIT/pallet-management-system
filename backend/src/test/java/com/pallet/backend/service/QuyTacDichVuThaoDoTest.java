package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuyTacDichVuThaoDoTest {

    @Test
    void tiepNhan() {
        assertDoesNotThrow(() -> QuyTacDichVuThaoDo.kiemTraTiepNhan(50, new BigDecimal("30000")));
        assertDoesNotThrow(() -> QuyTacDichVuThaoDo.kiemTraTiepNhan(1, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraTiepNhan(0, BigDecimal.TEN));
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraTiepNhan(5, new BigDecimal("-1")));
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraTiepNhan(5, null));
    }

    @Test
    void ketQua() {
        assertDoesNotThrow(() -> QuyTacDichVuThaoDo.kiemTraKetQua(50, 40, 70, 50, 8, 2, 20));
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraKetQua(50, 60, 1, 0, 0, 0, 0)); // vượt số nhận
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraKetQua(50, 40, 0, 0, 0, 0, 0));  // không thu được gì (TC10)
        assertThrows(BusinessException.class, () -> QuyTacDichVuThaoDo.kiemTraKetQua(50, 0, 5, 0, 0, 0, 0));   // 0 pallet
    }

    @Test
    void tinhPhi() {
        assertEquals(new BigDecimal("1200000.00"), QuyTacDichVuThaoDo.tinhPhi(40, new BigDecimal("30000")));
        assertEquals(new BigDecimal("0.00"), QuyTacDichVuThaoDo.tinhPhi(10, BigDecimal.ZERO));
    }
}