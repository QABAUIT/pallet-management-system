package com.pallet.backend.service;

import com.pallet.backend.dto.request.NhanVienRequest;
import com.pallet.backend.dto.request.TaoTaiKhoanRequest;
import com.pallet.backend.dto.response.NhanVienResponse;
import com.pallet.backend.dto.response.TaoTaiKhoanResponse;

import java.util.List;

public interface NhanVienService {
    List<NhanVienResponse> layDanhSach(String trangThai);

    NhanVienResponse layChiTiet(Long id);

    NhanVienResponse capNhat(Long id, NhanVienRequest request);

    void choNghiViec(Long id);

    TaoTaiKhoanResponse taoTaiKhoan(NhanVienRequest req);
}
