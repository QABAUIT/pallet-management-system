package com.pallet.backend.service;

import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.TonKhoKhach;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.TonKhoKhachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Sổ tồn hàng của khách gửi: tách hoàn toàn khỏi ton_kho của công ty (TC08). */
@Component
@RequiredArgsConstructor
public class TonKhoKhachNghiepVu {

    private final TonKhoKhachRepository tonKhoKhachRepository;

    public void cong(KhachHang khach, MatHang mh, Kho kho, int soLuong) {
        if (soLuong <= 0) return;
        TonKhoKhach t = tonKhoKhachRepository.findForUpdate(khach.getId(), mh.getId(), kho.getId())
                .orElseGet(() -> TonKhoKhach.builder().khachHang(khach).matHang(mh).kho(kho).soLuong(0).build());
        t.setSoLuong(t.getSoLuong() + soLuong);
        tonKhoKhachRepository.save(t);
    }

    public void tru(KhachHang khach, MatHang mh, Kho kho, int soLuong) {
        if (soLuong <= 0) return;
        TonKhoKhach t = tonKhoKhachRepository.findForUpdate(khach.getId(), mh.getId(), kho.getId())
                .orElseThrow(() -> khongDu(khach, mh, 0, soLuong));
        if (t.getSoLuong() < soLuong) {
            throw khongDu(khach, mh, t.getSoLuong(), soLuong);
        }
        t.setSoLuong(t.getSoLuong() - soLuong);
        tonKhoKhachRepository.save(t);
    }

    private BusinessException khongDu(KhachHang khach, MatHang mh, int dangCo, int can) {
        return new BusinessException("Hàng của khách " + khach.getTenKh() + " không đủ: " + mh.getTenMatHang()
                + " chỉ còn " + dangCo + ", cần " + can);
    }
}