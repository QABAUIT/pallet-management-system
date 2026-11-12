package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuThaoDoRequest;
import com.pallet.backend.dto.response.DanhMucThaoDoResponse;
import com.pallet.backend.dto.response.PhieuThaoDoResponse;

import java.util.List;

public interface PhieuThaoDoService {
    List<PhieuThaoDoResponse> layDanhSach(Long phieuTaiCheId, String tenDangNhap);
    PhieuThaoDoResponse layChiTiet(Long id, String tenDangNhap);
    DanhMucThaoDoResponse layDanhMuc(String tenDangNhap);
    PhieuThaoDoResponse taoMoi(TaoPhieuThaoDoRequest req, String tenDangNhap);
    PhieuThaoDoResponse capNhat(Long id, TaoPhieuThaoDoRequest req, String tenDangNhap);
    PhieuThaoDoResponse duyet(Long id, String tenDangNhap);
    PhieuThaoDoResponse huy(Long id, String lyDo, String tenDangNhap);
}