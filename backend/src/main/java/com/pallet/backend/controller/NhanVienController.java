package com.pallet.backend.controller;

import com.pallet.backend.dto.request.NhanVienRequest;
import com.pallet.backend.dto.response.NhanVienResponse;
import com.pallet.backend.service.NhanVienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienService nhanVienService;

    @GetMapping
    public ResponseEntity<List<NhanVienResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai) {
        return ResponseEntity.ok(nhanVienService.layDanhSach(trangThai));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienResponse> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(nhanVienService.layChiTiet(id));
    }

    @PostMapping
    public ResponseEntity<NhanVienResponse> taoMoi(@Valid @RequestBody NhanVienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nhanVienService.taoMoi(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NhanVienResponse> capNhat(
            @PathVariable Long id,
            @Valid @RequestBody NhanVienRequest request) {
        return ResponseEntity.ok(nhanVienService.capNhat(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> choNghiViec(@PathVariable Long id) {
        nhanVienService.choNghiViec(id);
        return ResponseEntity.noContent().build();
    }
}
