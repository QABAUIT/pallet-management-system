package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
import com.pallet.backend.dto.response.PhieuNhapResponse;
import com.pallet.backend.dto.response.ThongKePhieuNhapResponse;
import com.pallet.backend.dto.request.XepViTriRequest;
import com.pallet.backend.service.PhieuNhapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/phieu-nhap")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuNhapController {

    // khoId không gửi lên thì mặc định kho 1 (khớp KHO_ID trong frontend/src/config/kho.js)
    private static final String KHO_MAC_DINH = "1";

    private final PhieuNhapService phieuNhapService;

    @GetMapping("/thong-ke")
    public ResponseEntity<ThongKePhieuNhapResponse> getThongKe(
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId) {
        return ResponseEntity.ok(phieuNhapService.layThongKe(khoId));
    }

    @GetMapping
    public ResponseEntity<Page<PhieuNhapResponse>> getDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay,
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Mặc định sắp xếp phiếu mới nhất lên đầu
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayGio"));
        return ResponseEntity.ok(phieuNhapService.layDanhSach(keyword, trangThai, tuNgay, denNgay, khoId, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuNhapResponse> getChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(phieuNhapService.layChiTiet(id));
    }

    @PostMapping
    public ResponseEntity<PhieuNhapResponse> taoPhieuNhap(@Valid @RequestBody TaoPhieuNhapRequest request,
                                                          Principal principal) {
        return ResponseEntity.ok(phieuNhapService.taoPhieuNhap(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhieuNhapResponse> capNhatPhieuNhap(@PathVariable Long id,
                                                              @Valid @RequestBody TaoPhieuNhapRequest request,
                                                              Principal principal) {
        return ResponseEntity.ok(phieuNhapService.capNhatPhieuNhap(id, request, principal.getName()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> xuatExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay,
            @RequestParam(defaultValue = KHO_MAC_DINH) Long khoId) {

        byte[] excelContent = phieuNhapService.xuatExcel(keyword, trangThai, tuNgay, denNgay, khoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Lich_Su_Nhap_Kho.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }

    @PostMapping("/{id}/xep-vi-tri")
    public ResponseEntity<?> xepViTri(@PathVariable Long id, @RequestBody XepViTriRequest request,
                                      Principal principal) {
        phieuNhapService.xepViTri(id, request, principal.getName());
        return ResponseEntity.ok(java.util.Map.of("message", "Xếp vị trí lưu trữ thành công"));
    }
}