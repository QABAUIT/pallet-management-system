package com.pallet.backend.service;

import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class PhanQuyenKho {

    public boolean laQuanLy(NhanVien nv) {
        String ma = nv.getVaiTro() != null ? nv.getVaiTro().getMaVaiTro() : null;
        return "GD".equals(ma) || "PGD".equals(ma);
    }

    public boolean duocThaoTac(NhanVien nv, Kho kho) {
        if (laQuanLy(nv)) return true;
        return nv.getKho() != null && kho != null && nv.getKho().getId().equals(kho.getId());
    }

    public void kiemTra(NhanVien nv, Kho kho) {
        if (!duocThaoTac(nv, kho)) {
            throw new BusinessException("Bạn chỉ được thao tác với dữ liệu của kho được gán cho mình");
        }
    }
}