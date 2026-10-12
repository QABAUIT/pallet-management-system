package com.pallet.backend.service;

import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
import com.pallet.backend.dto.response.PhieuXuatResponse;
import com.pallet.backend.dto.response.ThongKePhieuXuatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PhieuXuatService {
    ThongKePhieuXuatResponse layThongKe();
    Page<PhieuXuatResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable);
    PhieuXuatResponse layChiTiet(Long id);
    PhieuXuatResponse taoPhieuXuat(TaoPhieuXuatRequest request);
    byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay);
}