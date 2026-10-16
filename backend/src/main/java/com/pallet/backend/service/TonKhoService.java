package com.pallet.backend.service;

import com.pallet.backend.dto.response.ThongKeTonKhoResponse;
import com.pallet.backend.dto.response.TonKhoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TonKhoService {
    ThongKeTonKhoResponse layThongKeTonKho(Long khoId);
    Page<TonKhoResponse> layDanhSachTonKho(String keyword, String chatLieu, Long khoId, Pageable pageable);
    byte[] xuatExcelBaoCaoTonKho(String keyword, String chatLieu, Long khoId);
    
    // Đã thêm 2 phương thức này để Controller gọi được
    List<Map<String, Object>> layDanhSachKho();
    List<Map<String, Object>> layDanhSachViTriKho(Long khoId);
}