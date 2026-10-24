package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuXuLyThemRequest;
import com.pallet.backend.dto.response.PhieuXuLyThemResponse;
import com.pallet.backend.dto.response.TonCanXuLyResponse;

import java.util.List;

public interface PhieuXuLyThemService {
    List<PhieuXuLyThemResponse> layDanhSach(String trangThai, String tenDangNhap);
    PhieuXuLyThemResponse layChiTiet(Long id, String tenDangNhap);
    TonCanXuLyResponse layTonCanXuLy(String tenDangNhap);
    PhieuXuLyThemResponse taoMoi(TaoPhieuXuLyThemRequest req, String tenDangNhap);
    PhieuXuLyThemResponse capNhat(Long id, TaoPhieuXuLyThemRequest req, String tenDangNhap);
    PhieuXuLyThemResponse duyet(Long id, String tenDangNhap);
    PhieuXuLyThemResponse huy(Long id, String lyDo, String tenDangNhap);
}