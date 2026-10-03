package com.pallet.backend.controller;

import com.pallet.backend.dto.request.NhanVienRequest;
import com.pallet.backend.dto.request.TaoTaiKhoanRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.NhanVienResponse;
import com.pallet.backend.dto.response.TaoTaiKhoanResponse;
import com.pallet.backend.service.NhanVienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienService nhanVienService;

    @GetMapping
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ResponseEntity<List<NhanVienResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai) {
        return ResponseEntity.ok(nhanVienService.layDanhSach(trangThai));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ResponseEntity<NhanVienResponse> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(nhanVienService.layChiTiet(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<TaoTaiKhoanResponse> taoTaiKhoan(@Valid @RequestBody TaoTaiKhoanRequest req) {
        return ApiResponse.success(nhanVienService.taoTaiKhoan(req), "Tạo tài khoản thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ResponseEntity<NhanVienResponse> capNhat(
            @PathVariable Long id,
            @Valid @RequestBody NhanVienRequest request) {
        return ResponseEntity.ok(nhanVienService.capNhat(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ResponseEntity<Void> choNghiViec(@PathVariable Long id) {
        nhanVienService.choNghiViec(id);
        return ResponseEntity.noContent().build();
    }
}