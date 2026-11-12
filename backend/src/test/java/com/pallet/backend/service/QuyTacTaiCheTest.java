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

        @Test
    void xuLyThemHopLe() {
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraXuLyThem("thanh", 4, 2, 3, "Bào lại mép"));
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraXuLyThem("cuc", 5, 0, 0, "Cắt lại cho vuông"));
        assertDoesNotThrow(() -> QuyTacTaiChe.kiemTraXuLyThem("cuc", 0, 3, 2, "Mục hết"));
    }

    @Test
    void xuLyThemSaiThiBiTuChoi() {
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("thanh", 4, 0, 0, " ")); // thiếu lý do
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("thanh", 0, 0, 0, "x")); // không có gì
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("thanh", 0, 2, 0, "x")); // bỏ mà không có kg
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("thanh", 3, 0, 5, "x")); // có kg mà không bỏ
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("go", 1, 0, 0, "x"));    // sai loại nguồn
        assertThrows(BusinessException.class, () -> QuyTacTaiChe.kiemTraXuLyThem("thanh", -1, 2, 1, "x"));
    }

    @Test
    void maNguonVaMaDich() {
        assertEquals("LK-TX-11", QuyTacTaiChe.maNguon("thanh"));
        assertEquals("LK-TN-09", QuyTacTaiChe.maDich("thanh"));
        assertEquals("LK-CX-12", QuyTacTaiChe.maNguon("cuc"));
        assertEquals("LK-CV-10", QuyTacTaiChe.maDich("cuc"));
    }
}