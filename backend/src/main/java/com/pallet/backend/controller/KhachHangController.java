package com.pallet.backend.controller;

import com.pallet.backend.dto.request.KhachHangRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.KhachHangResponse;
import com.pallet.backend.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVBANHANG')")
public class KhachHangController {

    private final KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai) {
        return ResponseEntity.ok(khachHangService.layDanhSach(trangThai));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHangResponse> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.layChiTiet(id));
    }

    @PostMapping
    public ApiResponse<KhachHangResponse> taoMoi(@Valid @RequestBody KhachHangRequest req) {
        return ApiResponse.success(khachHangService.taoMoi(req), "Thêm khách hàng thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhachHangResponse> capNhat(
            @PathVariable Long id,
            @Valid @RequestBody KhachHangRequest req) {
        return ResponseEntity.ok(khachHangService.capNhat(id, req));
    }
}