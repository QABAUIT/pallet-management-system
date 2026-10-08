package com.pallet.backend.service;

import com.pallet.backend.dto.request.CapNhatHoaDonRequest;
import com.pallet.backend.dto.request.TaoHoaDonRequest;
import com.pallet.backend.dto.request.ThanhToanHoaDonRequest;
import com.pallet.backend.dto.response.DanhMucBanHangResponse;
import com.pallet.backend.dto.response.HoaDonChiTietResponse;
import com.pallet.backend.dto.response.HoaDonResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;

import java.util.List;

public interface HoaDonService {
    List<HoaDonResponse> layDanhSach(String trangThai);

    HoaDonChiTietResponse layChiTiet(Long id);

    DanhMucBanHangResponse layDanhMucBanHang(String tenDangNhap);

    TaoHoaDonResponse taoMoi(TaoHoaDonRequest req, String tenDangNhap);

    void thanhToan(Long id, ThanhToanHoaDonRequest req, String tenDangNhap);

    void capNhat(Long id, CapNhatHoaDonRequest req, String tenDangNhap);

    void huy(Long id, String lyDo, String tenDangNhap);
}