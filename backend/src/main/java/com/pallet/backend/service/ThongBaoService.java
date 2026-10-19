package com.pallet.backend.service;

import com.pallet.backend.dto.response.ThongBaoResponse;
import com.pallet.backend.dto.response.ThongBaoThongKeResponse;

import java.util.List;

public interface ThongBaoService {

    List<ThongBaoResponse> layDanhSach(Boolean daDoc, String loaiThongBao, String username);

    ThongBaoThongKeResponse thongKe(String username);

    ThongBaoResponse layChiTiet(Long id);

    ThongBaoResponse danhDauDaDoc(Long id, Boolean daDoc);

    void danhDauTatCaDaDoc(String username);

    void xoa(Long id);
}
