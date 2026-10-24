package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuTaiCheRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucTaiCheResponse;
import com.pallet.backend.dto.response.PhieuTaiCheResponse;
import com.pallet.backend.service.PhieuTaiCheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tai-che")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuTaiCheController {

    private final PhieuTaiCheService phieuTaiCheService;

    @GetMapping
    public ApiResponse<List<PhieuTaiCheResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.layDanhSach(trangThai, principal.getName()), "OK");
    }

    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucTaiCheResponse> danhMuc(Principal principal) {
        return ApiResponse.success(phieuTaiCheService.layDanhMuc(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<PhieuTaiCheResponse> layChiTiet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.layChiTiet(id, principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<PhieuTaiCheResponse> taoMoi(@Valid @RequestBody TaoPhieuTaiCheRequest req, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.taoMoi(req, principal.getName()), "Tạo phiếu tái chế thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<PhieuTaiCheResponse> capNhat(@PathVariable Long id,
                                                    @Valid @RequestBody TaoPhieuTaiCheRequest req, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.capNhat(id, req, principal.getName()), "Cập nhật phiếu thành công");
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<PhieuTaiCheResponse> duyet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.duyetPhanLoai(id, principal.getName()), "Đã duyệt phân loại");
    }

    @PostMapping("/{id}/huy")
    public ApiResponse<PhieuTaiCheResponse> huy(@PathVariable Long id,
                                                @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(phieuTaiCheService.huy(id, lyDo, principal.getName()), "Đã hủy phiếu tái chế");
    }
}