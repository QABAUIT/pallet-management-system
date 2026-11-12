package com.pallet.backend.service;

import com.pallet.backend.dto.request.NhanHangDonMuaRequest;
import com.pallet.backend.dto.request.TaoDonMuaRequest;
import com.pallet.backend.dto.response.DanhMucDonMuaResponse;
import com.pallet.backend.dto.response.DonMuaChiTietResponse;
import com.pallet.backend.dto.response.DonMuaResponse;

import java.util.List;

public interface DonMuaService {
    List<DonMuaResponse> layDanhSach(String trangThai);
    DonMuaChiTietResponse layChiTiet(Long id);
    DanhMucDonMuaResponse layDanhMuc(String tenDangNhap);
    DonMuaChiTietResponse taoMoi(TaoDonMuaRequest req, String tenDangNhap);
    DonMuaChiTietResponse capNhat(Long id, TaoDonMuaRequest req, String tenDangNhap);
    DonMuaChiTietResponse xacNhan(Long id, String tenDangNhap);
    DonMuaChiTietResponse nhanHang(Long id, NhanHangDonMuaRequest req, String tenDangNhap);
    DonMuaChiTietResponse huy(Long id, String lyDo, String tenDangNhap);
}