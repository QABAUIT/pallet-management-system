package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuyTacDongPalletTest {

    @Test
    void tinhNhuCau() {
        assertEquals(30, QuyTacDongPallet.tinhNhuCau(6, 5));
        assertEquals(45, QuyTacDongPallet.tinhNhuCau(9, 5));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.tinhNhuCau(6, 0));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.tinhNhuCau(Integer.MAX_VALUE, 2));
    }

    @Test
    void chenhLechPhaiCoLyDo() {
        assertDoesNotThrow(() -> QuyTacDongPallet.kiemTraDong("Thanh", 30, 30, null));
        assertDoesNotThrow(() -> QuyTacDongPallet.kiemTraDong("Thanh", 30, 32, "Hỏng 2 thanh khi đóng"));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDong("Thanh", 30, 32, " "));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDong("Thanh", 30, 28, null));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDong("Thanh", 30, -1, "x"));
    }

    @Test
    void vatTuNgoaiDinhMucPhaiCoLyDo() {
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDong("Đinh", 0, 10, null));
        assertDoesNotThrow(() -> QuyTacDongPallet.kiemTraDong("Đinh", 0, 10, "Gia cố thêm"));
    }

    @Test
    void phaiCoVatTu() {
        assertDoesNotThrow(() -> QuyTacDongPallet.kiemTraCoVatTu(1));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraCoVatTu(0));
    }

    @Test
    void kiemTraDinhMuc() {
        assertDoesNotThrow(() -> QuyTacDongPallet.kiemTraDinhMuc(List.of(1L, 2L), List.of(6, 9)));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDinhMuc(List.of(), List.of()));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDinhMuc(List.of(1L, 1L), List.of(6, 9)));
        assertThrows(BusinessException.class, () -> QuyTacDongPallet.kiemTraDinhMuc(List.of(1L, 2L), List.of(6, 0)));
    }
}