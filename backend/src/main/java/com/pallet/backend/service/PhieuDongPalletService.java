package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuDongPalletRequest;
import com.pallet.backend.dto.response.DanhMucDongPalletResponse;
import com.pallet.backend.dto.response.DuKienDongPalletResponse;
import com.pallet.backend.dto.response.PhieuDongPalletResponse;

import java.util.List;

public interface PhieuDongPalletService {
    List<PhieuDongPalletResponse> layDanhSach(String trangThai, String tenDangNhap);
    PhieuDongPalletResponse layChiTiet(Long id, String tenDangNhap);
    DanhMucDongPalletResponse layDanhMuc();
    DuKienDongPalletResponse duKien(Long dinhMucId, Long khoId, int soLuong, String tenDangNhap);
    PhieuDongPalletResponse taoMoi(TaoPhieuDongPalletRequest req, String tenDangNhap);
    PhieuDongPalletResponse capNhat(Long id, TaoPhieuDongPalletRequest req, String tenDangNhap);
    PhieuDongPalletResponse duyet(Long id, String tenDangNhap);
    PhieuDongPalletResponse huy(Long id, String lyDo, String tenDangNhap);
}