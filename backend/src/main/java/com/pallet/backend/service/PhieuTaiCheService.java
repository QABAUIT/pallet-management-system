package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuTaiCheRequest;
import com.pallet.backend.dto.response.DanhMucTaiCheResponse;
import com.pallet.backend.dto.response.PhieuTaiCheResponse;

import java.util.List;

public interface PhieuTaiCheService {
    List<PhieuTaiCheResponse> layDanhSach(String trangThai, String tenDangNhap);
    PhieuTaiCheResponse layChiTiet(Long id, String tenDangNhap);
    DanhMucTaiCheResponse layDanhMuc(String tenDangNhap);
    PhieuTaiCheResponse taoMoi(TaoPhieuTaiCheRequest req, String tenDangNhap);
    PhieuTaiCheResponse capNhat(Long id, TaoPhieuTaiCheRequest req, String tenDangNhap);
    PhieuTaiCheResponse duyetPhanLoai(Long id, String tenDangNhap);
    PhieuTaiCheResponse huy(Long id, String lyDo, String tenDangNhap);
}