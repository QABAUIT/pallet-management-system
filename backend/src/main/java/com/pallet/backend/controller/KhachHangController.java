package com.pallet.backend.controller;

import com.pallet.backend.dto.request.KhachHangRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.KhachHangResponse;
import com.pallet.backend.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: thêm @PreAuthorize khi có Spring Security, để giới hạn nhân viên nào
// được xem/sửa khách hàng (vd chỉ GD/PGD/sales, không phải mọi nhân viên kho).
@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
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