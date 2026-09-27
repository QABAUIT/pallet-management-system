package com.pallet.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pallet.backend.dto.request.MatHangRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.MatHangResponse;
import com.pallet.backend.service.MatHangService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mat-hang")
@RequiredArgsConstructor
public class MatHangController {

    private final MatHangService matHangService;

    @GetMapping
    public ApiResponse<List<MatHangResponse>> danhSach(
            @RequestParam(required = false) String trangThai) {
        return ApiResponse.success(
                matHangService.layDanhSach(trangThai),
                null
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<MatHangResponse> chiTiet(@PathVariable Long id) {
        return ApiResponse.success(
                matHangService.layChiTiet(id),
                null
        );
    }

    @PostMapping
    public ApiResponse<MatHangResponse> taoMoi(
            @Valid @RequestBody MatHangRequest request) {
        return ApiResponse.success(
                matHangService.taoMoi(request),
                "Tạo mặt hàng thành công"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<MatHangResponse> capNhat(
            @PathVariable Long id,
            @Valid @RequestBody MatHangRequest request) {
        return ApiResponse.success(
                matHangService.capNhat(id, request),
                "Cập nhật thành công"
        );
    }

    @PutMapping("/{id}/ngung-kinh-doanh")
    public ApiResponse<Void> ngungKinhDoanh(
            @PathVariable Long id) {
        matHangService.ngungKinhDoanh(id);

        return ApiResponse.success(
                null,
                "Đã ngừng kinh doanh mặt hàng"
        );
    }
}