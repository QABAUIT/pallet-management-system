package com.pallet.backend.controller;

import com.pallet.backend.dto.request.CapNhatChiTietKiemKeRequest;
import com.pallet.backend.dto.request.TaoPhienKiemKeRequest;
import com.pallet.backend.dto.response.ChiTietKiemKeResponse;
import com.pallet.backend.dto.response.ThongKeKiemKeResponse;
import com.pallet.backend.entity.PhienKiemKe;
import com.pallet.backend.service.KiemKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kiem-ke")
@RequiredArgsConstructor
public class KiemKeController {

    private final KiemKeService kiemKeService;

    @PostMapping("/phien")
public ResponseEntity<Map<String, Object>> taoPhienKiemKe(@RequestBody TaoPhienKiemKeRequest request) {
    PhienKiemKe p = kiemKeService.taoPhienKiemKe(request);
    return ResponseEntity.ok(Map.of(
            "id", p.getId(),
            "maPhien", p.getMaPhien(),
            "trangThai", p.getTrangThai()));
}

    @GetMapping("/phien/{id}/chi-tiet")
    public ResponseEntity<List<ChiTietKiemKeResponse>> getChiTietPhien(@PathVariable Long id) {
        return ResponseEntity.ok(kiemKeService.layDanhSachChiTiet(id));
    }

    @GetMapping("/phien/{id}/thong-ke")
    public ResponseEntity<ThongKeKiemKeResponse> getThongKe(@PathVariable Long id) {
        return ResponseEntity.ok(kiemKeService.layThongKe(id));
    }

    @PutMapping("/chi-tiet/{chiTietId}")
    public ResponseEntity<?> capNhatChiTiet(@PathVariable Long chiTietId, @RequestBody CapNhatChiTietKiemKeRequest request) {
        kiemKeService.capNhatChiTiet(chiTietId, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
    }

    @PostMapping("/phien/{id}/hoan-tat")
    public ResponseEntity<?> hoanTatKiemKe(@PathVariable Long id, @RequestParam Long nguoiChotId) {
        try {
            kiemKeService.hoanTatKiemKe(id, nguoiChotId);
            return ResponseEntity.ok(Map.of("message", "Chốt phiên kiểm kê thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}