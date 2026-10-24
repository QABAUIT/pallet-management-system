package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;

import java.math.BigDecimal;

/** Quy tắc dịch vụ tháo dỡ cho khách (hàm thuần). */
public final class QuyTacDichVuThaoDo {

    private QuyTacDichVuThaoDo() {
    }

    public static void kiemTraTiepNhan(int soPalletNhan, BigDecimal donGia) {
        if (soPalletNhan <= 0) {
            throw new BusinessException("Số pallet nhận phải lớn hơn 0");
        }
        if (donGia == null || donGia.signum() < 0) {
            throw new BusinessException("Đơn giá dịch vụ không được âm");
        }
    }

    /** Kết quả tháo dỡ: ít nhất 1 pallet, không vượt số nhận, phải thu được ít nhất một thứ (TC10). */
    public static void kiemTraKetQua(int soPalletNhan, int soPalletThaoDo, int thanhNguyen, int cucVuong,
                                     int thanhCanXuLy, int cucCanXuLy, int kgPheLieu) {
        if (soPalletThaoDo > soPalletNhan) {
            throw new BusinessException("Số pallet tháo dỡ (" + soPalletThaoDo
                    + ") vượt số pallet đã nhận (" + soPalletNhan + ")");
        }
        QuyTacTaiChe.kiemTraThaoDo(soPalletThaoDo, thanhNguyen, cucVuong, thanhCanXuLy, cucCanXuLy, kgPheLieu);
    }

    /** Phí dịch vụ = số pallet đã tháo dỡ x đơn giá thỏa thuận. */
    public static BigDecimal tinhPhi(int soPalletThaoDo, BigDecimal donGia) {
        return donGia.multiply(BigDecimal.valueOf(soPalletThaoDo)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}