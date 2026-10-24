package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoDinhMucRequest;
import com.pallet.backend.dto.response.DinhMucResponse;

import java.util.List;

public interface DinhMucService {
    List<DinhMucResponse> layDanhSach(boolean chiHoatDong);
    DinhMucResponse layChiTiet(Long id);
    DinhMucResponse taoMoi(TaoDinhMucRequest req, String tenDangNhap);
    DinhMucResponse capNhat(Long id, TaoDinhMucRequest req, String tenDangNhap);
    DinhMucResponse doiTrangThai(Long id, boolean hoatDong, String tenDangNhap);
}