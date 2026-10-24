package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoDinhMucRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DinhMucResponse;
import com.pallet.backend.service.DinhMucService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dinh-muc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class DinhMucController {

    private final DinhMucService dinhMucService;

    @GetMapping
    public ApiResponse<List<DinhMucResponse>> layDanhSach(@RequestParam(defaultValue = "false") boolean chiHoatDong) {
        return ApiResponse.success(dinhMucService.layDanhSach(chiHoatDong), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<DinhMucResponse> layChiTiet(@PathVariable Long id) {
        return ApiResponse.success(dinhMucService.layChiTiet(id), "OK");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DinhMucResponse> taoMoi(@Valid @RequestBody TaoDinhMucRequest req, Principal principal) {
        return ApiResponse.success(dinhMucService.taoMoi(req, principal.getName()), "Tạo định mức thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DinhMucResponse> capNhat(@PathVariable Long id,
                                                @Valid @RequestBody TaoDinhMucRequest req, Principal principal) {
        return ApiResponse.success(dinhMucService.capNhat(id, req, principal.getName()), "Cập nhật định mức thành công");
    }

    @PostMapping("/{id}/ngung")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DinhMucResponse> ngung(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(dinhMucService.doiTrangThai(id, false, principal.getName()), "Đã ngừng định mức");
    }

    @PostMapping("/{id}/kich-hoat")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DinhMucResponse> kichHoat(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(dinhMucService.doiTrangThai(id, true, principal.getName()), "Đã kích hoạt định mức");
    }
}