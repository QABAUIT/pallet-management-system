package com.pallet.backend.service;

import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.TonKho;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.TonKhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Cộng/trừ tồn kho theo đúng ràng buộc UNIQUE (mat_hang_id, kho_id): mỗi mặt hàng
 * chỉ có 1 dòng tồn trong mỗi kho. Phải gọi trong @Transactional (khóa dòng tồn).
 */
@Component
@RequiredArgsConstructor
public class TonKhoNghiepVu {

    private final TonKhoRepository tonKhoRepository;

    /** Cộng tồn; chưa có dòng tồn thì tạo mới. */
    public void cong(MatHang mh, Kho kho, int soLuong) {
        if (soLuong <= 0) {
            throw new BusinessException("Số lượng cộng vào kho phải lớn hơn 0");
        }
        TonKho t = tonKhoRepository.findForUpdate(mh.getId(), kho.getId())
                .orElseGet(() -> TonKho.builder()
                        .matHang(mh).kho(kho)
                        .soLuongTonKho(0).soLuongDaGiuCho(0).soLuongToiThieu(0)
                        .updatedAt(LocalDateTime.now())
                        .build());
        t.setSoLuongTonKho(t.getSoLuongTonKho() + soLuong);
        tonKhoRepository.save(t);
    }

    /** Trừ tồn; không cho trừ quá số khả dụng (tồn trừ hàng đang giữ chỗ). */
    public void tru(MatHang mh, Kho kho, int soLuong) {
        if (soLuong <= 0) {
            throw new BusinessException("Số lượng trừ khỏi kho phải lớn hơn 0");
        }
        TonKho t = tonKhoRepository.findForUpdate(mh.getId(), kho.getId())
                .orElseThrow(() -> new BusinessException(
                        mh.getTenMatHang() + " chưa có tồn kho tại " + kho.getTenKho()));
        int khaDung = t.getSoLuongTonKho() - t.getSoLuongDaGiuCho();
        if (khaDung < soLuong) {
            throw new BusinessException("Tồn kho khả dụng của " + mh.getTenMatHang()
                    + " tại " + kho.getTenKho() + " chỉ còn " + khaDung);
        }
        t.setSoLuongTonKho(t.getSoLuongTonKho() - soLuong);
        tonKhoRepository.save(t);
    }
}