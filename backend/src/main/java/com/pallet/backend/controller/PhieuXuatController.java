package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
import com.pallet.backend.dto.response.PhieuXuatResponse;
import com.pallet.backend.dto.response.ThongKePhieuXuatResponse;
import com.pallet.backend.service.PhieuXuatService;
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
@RequestMapping("/api/v1/phieu-xuat")
@RequiredArgsConstructor
public class PhieuXuatController {

    private final PhieuXuatService phieuXuatService;

    @GetMapping("/thong-ke")
    public ResponseEntity<ThongKePhieuXuatResponse> getThongKe() {
        return ResponseEntity.ok(phieuXuatService.layThongKe());
    }

    @GetMapping
    public ResponseEntity<Page<PhieuXuatResponse>> getDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayGio"));
        return ResponseEntity.ok(phieuXuatService.layDanhSach(keyword, trangThai, tuNgay, denNgay, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuXuatResponse> getChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(phieuXuatService.layChiTiet(id));
    }

    @PostMapping
    public ResponseEntity<PhieuXuatResponse> taoPhieuXuat(@RequestBody TaoPhieuXuatRequest request) {
        return ResponseEntity.ok(phieuXuatService.taoPhieuXuat(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> xuatExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay) {

        byte[] excelContent = phieuXuatService.xuatExcel(keyword, trangThai, tuNgay, denNgay);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Lich_Su_Xuat_Kho.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}