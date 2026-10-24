package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuXuLyThemRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.PhieuXuLyThemResponse;
import com.pallet.backend.dto.response.TonCanXuLyResponse;
import com.pallet.backend.service.PhieuXuLyThemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/xu-ly-them")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuXuLyThemController {

    private final PhieuXuLyThemService phieuXuLyThemService;

    @GetMapping
    public ApiResponse<List<PhieuXuLyThemResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.layDanhSach(trangThai, principal.getName()), "OK");
    }

    @GetMapping("/ton")
    public ApiResponse<TonCanXuLyResponse> tonCanXuLy(Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.layTonCanXuLy(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<PhieuXuLyThemResponse> layChiTiet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.layChiTiet(id, principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<PhieuXuLyThemResponse> taoMoi(@Valid @RequestBody TaoPhieuXuLyThemRequest req, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.taoMoi(req, principal.getName()), "Tạo phiếu xử lý thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<PhieuXuLyThemResponse> capNhat(@PathVariable Long id,
                                                      @Valid @RequestBody TaoPhieuXuLyThemRequest req, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.capNhat(id, req, principal.getName()), "Cập nhật phiếu thành công");
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<PhieuXuLyThemResponse> duyet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.duyet(id, principal.getName()), "Đã duyệt phiếu xử lý");
    }

    @PostMapping("/{id}/huy")
    public ApiResponse<PhieuXuLyThemResponse> huy(@PathVariable Long id,
                                                  @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(phieuXuLyThemService.huy(id, lyDo, principal.getName()), "Đã hủy phiếu xử lý");
    }
}