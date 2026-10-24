package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuyTacTaiCheTest {

    @Test
    void phanLoaiDungThiHopLe() {
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraPhanLoai(60, 20, 30, 10, 25));
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraPhanLoai(40, 40, 0, 0, 0));
    }

    @Test
    void tongBaNhomLechThiBiTuChoi() {
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(60, 20, 30, 9, 25));
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(60, 20, 30, 11, 25));
    }

    @Test
    void loaiBoPhaiCoKgPheLieu() {
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(10, 0, 0, 10, 0));
    }

    @Test
    void khongCoLoaiBoThiKhongDuocCoKg() {
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(10, 10, 0, 0, 5));
    }

    @Test
    void soAmHoacSoXuLyKhongHopLeBiTuChoi() {
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(0, 0, 0, 0, 0));
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(10, -1, 6, 5, 3));
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraPhanLoai(10, 10, 0, 0, -1));
    }

    @Test
    void conPalletThaoDoThiChuaHoanTat() {
        assertEquals("da_phan_loai", QuyTacTaiChe.trangThaiSauDuyet(30));
        assertEquals("da_hoan_tat", QuyTacTaiChe.trangThaiSauDuyet(0));
    }

    @Test
    void thaoDoPhaiCoKetQuaVaKhongAm() {
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraThaoDo(20, 40, 30, 6, 4, 12));
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraThaoDo(5, 0, 0, 0, 0, 3)); // chỉ ra phế liệu vẫn hợp lệ
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraThaoDo(5, 0, 0, 0, 0, 0)); // TC10
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraThaoDo(0, 1, 0, 0, 0, 0));
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraThaoDo(5, -1, 2, 0, 0, 0));
    }

    @Test
    void trangThaiSauThaoDo() {
        assertEquals("da_phan_loai", QuyTacTaiChe.trangThaiSauThaoDo(20, 30));
        assertEquals("da_hoan_tat", QuyTacTaiChe.trangThaiSauThaoDo(30, 30));
    }
}