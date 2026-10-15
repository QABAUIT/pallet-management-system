package com.pallet.backend.service;

import com.pallet.backend.dto.response.ThongKeTonKhoResponse;
import com.pallet.backend.dto.response.TonKhoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TonKhoService {
    ThongKeTonKhoResponse layThongKeTonKho(Long khoId);
    Page<TonKhoResponse> layDanhSachTonKho(String keyword, String chatLieu, Long khoId, Pageable pageable);
    byte[] xuatExcelBaoCaoTonKho(String keyword, String chatLieu, Long khoId);
}