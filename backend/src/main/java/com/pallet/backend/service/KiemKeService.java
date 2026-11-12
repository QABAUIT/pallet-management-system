package com.pallet.backend.service;

import com.pallet.backend.dto.request.CapNhatChiTietKiemKeRequest;
import com.pallet.backend.dto.request.TaoPhienKiemKeRequest;
import com.pallet.backend.dto.response.ChiTietKiemKeResponse;
import com.pallet.backend.dto.response.ThongKeKiemKeResponse;
import com.pallet.backend.entity.PhienKiemKe;

import java.util.List;

public interface KiemKeService {
    PhienKiemKe taoPhienKiemKe(TaoPhienKiemKeRequest request, String tenDangNhap);
    List<ChiTietKiemKeResponse> layDanhSachChiTiet(Long phienKiemKeId);
    ThongKeKiemKeResponse layThongKe(Long phienKiemKeId);
    void capNhatChiTiet(Long chiTietId, CapNhatChiTietKiemKeRequest request, String tenDangNhap);
    void hoanTatKiemKe(Long phienKiemKeId, String tenDangNhap);
}