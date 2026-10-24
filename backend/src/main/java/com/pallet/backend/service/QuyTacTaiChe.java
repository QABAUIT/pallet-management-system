package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;

/** Quy tắc nghiệp vụ tái chế (hàm thuần, không phụ thuộc DB) + mã các mặt hàng hệ thống. */
public final class QuyTacTaiChe {

    public static final String MA_CHO_PHAN_LOAI = "PL-CPL-05"; // Pallet gỗ cũ chờ phân loại
    public static final String MA_CHO_THAO_DO = "PL-CTD-06";   // Pallet gỗ cũ chờ tháo dỡ
    public static final String MA_DAT_CHUAN = "PL-DC-07";      // Pallet gỗ cũ đạt chuẩn
    public static final String MA_PHE_LIEU = "PE-GO-15";       // Phế liệu gỗ (kg)
    public static final String MA_THANH_NGUYEN = "LK-TN-09";
    public static final String MA_CUC_VUONG = "LK-CV-10";
    public static final String MA_THANH_CAN_XU_LY = "LK-TX-11";
    public static final String MA_CUC_CAN_XU_LY = "LK-CX-12";

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
        /** Một đợt tháo dỡ: số pallet > 0, các số lượng không âm, phải thu được ít nhất một thứ (TC10). */
    public static void kiemTraThaoDo(int soPallet, int thanhNguyen, int cucVuong,
                                     int thanhCanXuLy, int cucCanXuLy, int kgPheLieu) {
        if (soPallet <= 0) {
            throw new BusinessException("Số pallet tháo dỡ phải lớn hơn 0");
        }
        if (thanhNguyen < 0 || cucVuong < 0 || thanhCanXuLy < 0 || cucCanXuLy < 0 || kgPheLieu < 0) {
            throw new BusinessException("Số lượng kết quả tháo dỡ không được âm");
        }
        if (thanhNguyen + cucVuong + thanhCanXuLy + cucCanXuLy + kgPheLieu == 0) {
            throw new BusinessException("Đợt tháo dỡ phải thu được ít nhất một loại linh kiện hoặc phế liệu. "
                    + "Pallet hỏng hoàn toàn cần được phân loại vào nhóm loại bỏ");
        }
    }

    /** Đủ số pallet cần tháo dỡ thì phiếu tái chế hoàn tất. */
    public static String trangThaiSauThaoDo(int tongDaDuyet, int soCanThaoDo) {
        return tongDaDuyet >= soCanThaoDo ? "da_hoan_tat" : "da_phan_loai";
    }
}