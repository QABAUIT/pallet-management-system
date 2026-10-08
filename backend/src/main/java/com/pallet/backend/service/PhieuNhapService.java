package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
import com.pallet.backend.dto.response.PhieuNhapResponse;
import com.pallet.backend.dto.response.ThongKePhieuNhapResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PhieuNhapService {
    ThongKePhieuNhapResponse layThongKe();
    Page<PhieuNhapResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable);
    PhieuNhapResponse layChiTiet(Long id);
    PhieuNhapResponse taoPhieuNhap(TaoPhieuNhapRequest request);
    byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay);
}