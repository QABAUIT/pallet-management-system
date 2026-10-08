package com.pallet.backend.controller;

import com.pallet.backend.dto.response.ThongKeTonKhoResponse;
import com.pallet.backend.dto.response.TonKhoResponse;
import com.pallet.backend.service.TonKhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/ton-kho")
@RequiredArgsConstructor
public class TonKhoController {

    private final TonKhoService tonKhoService;

    // API lấy biểu đồ/thống kê tổng quan
    @GetMapping("/thong-ke")
    public ResponseEntity<ThongKeTonKhoResponse> getThongKe() {
        ThongKeTonKhoResponse thongKe = tonKhoService.layThongKeTonKho();
        return ResponseEntity.ok(thongKe);
    }

    // API lấy danh sách chi tiết có search, filter, pagination
    @GetMapping
    public ResponseEntity<Page<TonKhoResponse>> getDanhSachTonKho(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chatLieu,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TonKhoResponse> result = tonKhoService.layDanhSachTonKho(keyword, chatLieu, pageable);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> xuatExcelBaoCao(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chatLieu) {
        
        byte[] excelContent = tonKhoService.xuatExcelBaoCaoTonKho(keyword, chatLieu);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        // Trả file về Frontend với tên Bao_Cao_Ton_Kho.xlsx
        headers.setContentDispositionFormData("attachment", "Bao_Cao_Ton_Kho.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}