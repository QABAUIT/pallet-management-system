package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;

/** Quy tắc nghiệp vụ tái chế (hàm thuần, không phụ thuộc DB) + mã các mặt hàng hệ thống. */
public final class QuyTacTaiChe {

    public static final String MA_CHO_PHAN_LOAI = "PL-CPL-05"; // Pallet gỗ cũ chờ phân loại
    public static final String MA_CHO_THAO_DO = "PL-CTD-06";   // Pallet gỗ cũ chờ tháo dỡ
    public static final String MA_DAT_CHUAN = "PL-DC-07";      // Pallet gỗ cũ đạt chuẩn
    public static final String MA_PHE_LIEU = "PE-GO-15";       // Phế liệu gỗ (kg)

    private QuyTacTaiChe() {
    }

    /** 3 nhóm phải cộng đúng bằng số pallet xử lý; nhóm loại bỏ bắt buộc có kg phế liệu. */
    public static void kiemTraPhanLoai(int soXuLy, int banLai, int thaoDo, int loaiBo, int kgPheLieu) {
        if (soXuLy <= 0) {
            throw new BusinessException("Số pallet xử lý phải lớn hơn 0");
        }
        if (banLai < 0 || thaoDo < 0 || loaiBo < 0 || kgPheLieu < 0) {
            throw new BusinessException("Số lượng các nhóm và số kg không được âm");
        }
        int tong = banLai + thaoDo + loaiBo;
        if (tong != soXuLy) {
            throw new BusinessException("Tổng 3 nhóm (bán lại " + banLai + " + tháo dỡ " + thaoDo
                    + " + loại bỏ " + loaiBo + " = " + tong + ") phải bằng số pallet xử lý (" + soXuLy + ")");
        }
        if (loaiBo > 0 && kgPheLieu <= 0) {
            throw new BusinessException("Nhóm loại bỏ phải nhập số kg phế liệu thu được");
        }
        if (loaiBo == 0 && kgPheLieu > 0) {
            throw new BusinessException("Có nhập kg phế liệu nhưng không có pallet nào ở nhóm loại bỏ");
        }
    }

    /** Còn pallet cần tháo dỡ thì phiếu chưa xong, chờ bước tháo dỡ. */
    public static String trangThaiSauDuyet(int soThaoDo) {
        return soThaoDo > 0 ? "da_phan_loai" : "da_hoan_tat";
    }
}