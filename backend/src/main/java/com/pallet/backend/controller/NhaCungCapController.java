package com.pallet.backend.controller;

import com.pallet.backend.dto.request.NhaCungCapRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.NhaCungCapResponse;
import com.pallet.backend.dto.response.dto.PageResponse;
import com.pallet.backend.service.NhaCungCapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nha-cung-cap")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NhaCungCapController {

    private final NhaCungCapService nhaCungCapService;

    // GET /api/v1/nha-cung-cap
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String nhomHang) {

        PageResponse data = nhaCungCapService.getAll(page, size, keyword, nhomHang);
        return ResponseEntity.ok(ApiResponse.success(data, "Lấy danh sách nhà cung cấp thành công"));
    }

    // GET /api/v1/nha-cung-cap/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NhaCungCapResponse>> getById(@PathVariable Long id) {
        NhaCungCapResponse data = nhaCungCapService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(data, "Lấy chi tiết nhà cung cấp thành công"));
    }

    // POST /api/v1/nha-cung-cap
    @PostMapping
    public ResponseEntity<ApiResponse<NhaCungCapResponse>> create(@Valid @RequestBody NhaCungCapRequest request) {
        NhaCungCapResponse data = nhaCungCapService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Thêm mới nhà cung cấp thành công"));
    }

    // PUT /api/v1/nha-cung-cap/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NhaCungCapResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody NhaCungCapRequest request) {
        NhaCungCapResponse data = nhaCungCapService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(data, "Cập nhật nhà cung cấp thành công"));
    }

    // DELETE /api/v1/nha-cung-cap/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        nhaCungCapService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhà cung cấp thành công"));
    }
}