package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
import com.pallet.backend.dto.response.PhieuNhapResponse;
import com.pallet.backend.dto.response.ThongKePhieuNhapResponse;
import com.pallet.backend.service.PhieuNhapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/phieu-nhap")
@RequiredArgsConstructor
public class PhieuNhapController {

    private final PhieuNhapService phieuNhapService;

    @GetMapping("/thong-ke")
    public ResponseEntity<ThongKePhieuNhapResponse> getThongKe() {
        return ResponseEntity.ok(phieuNhapService.layThongKe());
    }

    @GetMapping
    public ResponseEntity<Page<PhieuNhapResponse>> getDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Mặc định sắp xếp phiếu mới nhất lên đầu
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayGio"));
        return ResponseEntity.ok(phieuNhapService.layDanhSach(keyword, trangThai, tuNgay, denNgay, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuNhapResponse> getChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(phieuNhapService.layChiTiet(id));
    }

    @PostMapping
    public ResponseEntity<PhieuNhapResponse> taoPhieuNhap(@RequestBody TaoPhieuNhapRequest request) {
        return ResponseEntity.ok(phieuNhapService.taoPhieuNhap(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> xuatExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay) {

        byte[] excelContent = phieuNhapService.xuatExcel(keyword, trangThai, tuNgay, denNgay);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Lich_Su_Nhap_Kho.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}