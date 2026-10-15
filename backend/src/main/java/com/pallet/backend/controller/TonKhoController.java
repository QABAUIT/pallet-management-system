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
    private static final String KHO_MAC_DINH = "1";

    @GetMapping("/thong-ke")
    public ResponseEntity<ThongKeTonKhoResponse> getThongKe(
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId) {
        ThongKeTonKhoResponse thongKe = tonKhoService.layThongKeTonKho(khoId);
        return ResponseEntity.ok(thongKe);
    }

    @GetMapping
    public ResponseEntity<Page<TonKhoResponse>> getDanhSachTonKho(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chatLieu,
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TonKhoResponse> result = tonKhoService.layDanhSachTonKho(keyword, chatLieu, khoId, pageable);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> xuatExcelBaoCao(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chatLieu,
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId) {
        
        byte[] excelContent = tonKhoService.xuatExcelBaoCaoTonKho(keyword, chatLieu, khoId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Bao_Cao_Ton_Kho.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}