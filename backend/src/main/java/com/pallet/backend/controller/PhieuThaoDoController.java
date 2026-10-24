package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuThaoDoRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucThaoDoResponse;
import com.pallet.backend.dto.response.PhieuThaoDoResponse;
import com.pallet.backend.service.PhieuThaoDoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/thao-do")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuThaoDoController {

    private final PhieuThaoDoService phieuThaoDoService;

    @GetMapping
    public ApiResponse<List<PhieuThaoDoResponse>> layDanhSach(
            @RequestParam(required = false) Long phieuTaiCheId, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.layDanhSach(phieuTaiCheId, principal.getName()), "OK");
    }

    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucThaoDoResponse> danhMuc(Principal principal) {
        return ApiResponse.success(phieuThaoDoService.layDanhMuc(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<PhieuThaoDoResponse> layChiTiet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.layChiTiet(id, principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<PhieuThaoDoResponse> taoMoi(@Valid @RequestBody TaoPhieuThaoDoRequest req, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.taoMoi(req, principal.getName()), "Tạo phiếu tháo dỡ thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<PhieuThaoDoResponse> capNhat(@PathVariable Long id,
                                                    @Valid @RequestBody TaoPhieuThaoDoRequest req, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.capNhat(id, req, principal.getName()), "Cập nhật phiếu thành công");
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<PhieuThaoDoResponse> duyet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.duyet(id, principal.getName()), "Đã duyệt phiếu tháo dỡ");
    }

    @PostMapping("/{id}/huy")
    public ApiResponse<PhieuThaoDoResponse> huy(@PathVariable Long id,
                                                @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(phieuThaoDoService.huy(id, lyDo, principal.getName()), "Đã hủy phiếu tháo dỡ");
    }
}