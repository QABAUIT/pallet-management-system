package com.pallet.backend.service;

import com.pallet.backend.dto.request.KetQuaDichVuRequest;
import com.pallet.backend.dto.request.TiepNhanDichVuRequest;
import com.pallet.backend.dto.response.PhieuDichVuThaoDoResponse;
import com.pallet.backend.dto.response.TonKhoKhachResponse;

import java.util.List;

public interface PhieuDichVuThaoDoService {
    List<PhieuDichVuThaoDoResponse> layDanhSach(String trangThai, String tenDangNhap);
    PhieuDichVuThaoDoResponse layChiTiet(Long id, String tenDangNhap);
    TonKhoKhachResponse layTonKhach(Long khachHangId, String tenDangNhap);
    PhieuDichVuThaoDoResponse tiepNhan(TiepNhanDichVuRequest req, String tenDangNhap);
    PhieuDichVuThaoDoResponse ghiNhanKetQua(Long id, KetQuaDichVuRequest req, String tenDangNhap);
    PhieuDichVuThaoDoResponse duyet(Long id, String tenDangNhap);
    PhieuDichVuThaoDoResponse traHang(Long id, String tenDangNhap);
    PhieuDichVuThaoDoResponse huy(Long id, String lyDo, String tenDangNhap);
}