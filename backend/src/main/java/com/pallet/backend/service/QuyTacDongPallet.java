package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;

/** Quy tắc đóng pallet theo định mức (hàm thuần, không phụ thuộc DB). */
public final class QuyTacDongPallet {

    private QuyTacDongPallet() {
    }

    /** Nhu cầu vật tư = định mức 1 pallet x số pallet sản xuất. */
    public static int tinhNhuCau(int dinhMucMotPallet, int soSanXuat) {
        if (soSanXuat <= 0) {
            throw new BusinessException("Số pallet sản xuất phải lớn hơn 0");
        }
        try {
            return Math.multiplyExact(dinhMucMotPallet, soSanXuat);
        } catch (ArithmeticException e) {
            throw new BusinessException("Số lượng sản xuất quá lớn");
        }
    }

    public static int chenhLech(int thucTe, int dinhMuc) {
        return thucTe - dinhMuc;
    }

    /** Dòng vật tư: không âm; lệch so với định mức thì bắt buộc có lý do (TC12). */
    public static void kiemTraDong(String tenVatTu, int dinhMuc, int thucTe, String lyDo) {
        if (thucTe < 0) {
            throw new BusinessException("Số lượng thực tế của " + tenVatTu + " không được âm");
        }
        if (thucTe != dinhMuc && (lyDo == null || lyDo.isBlank())) {
            throw new BusinessException("Vật tư " + tenVatTu + " dùng " + thucTe + " khác định mức " + dinhMuc
                    + ", bắt buộc nhập lý do chênh lệch");
        }
    }

    /** Phiếu phải dùng ít nhất một vật tư, không đóng pallet từ hư không. */
    public static void kiemTraCoVatTu(int tongThucTe) {
        if (tongThucTe <= 0) {
            throw new BusinessException("Phiếu đóng pallet phải dùng ít nhất một vật tư");
        }
    }

    /** Định mức: không rỗng, số lượng dương, không trùng vật tư. */
    public static void kiemTraDinhMuc(java.util.List<Long> matHangIds, java.util.List<Integer> soLuongs) {
        if (matHangIds == null || matHangIds.isEmpty()) {
            throw new BusinessException("Định mức phải có ít nhất một vật tư");
        }
        if (matHangIds.size() != new java.util.HashSet<>(matHangIds).size()) {
            throw new BusinessException("Định mức có vật tư bị trùng dòng");
        }
        for (Integer sl : soLuongs) {
            if (sl == null || sl <= 0) {
                throw new BusinessException("Số lượng vật tư trong định mức phải lớn hơn 0");
            }
        }
    }
}