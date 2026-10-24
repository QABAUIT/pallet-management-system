package com.pallet.backend.controller;

import com.pallet.backend.dto.request.TaoPhieuDongPalletRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucDongPalletResponse;
import com.pallet.backend.dto.response.DuKienDongPalletResponse;
import com.pallet.backend.dto.response.PhieuDongPalletResponse;
import com.pallet.backend.service.PhieuDongPalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dong-pallet")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuDongPalletController {

    private final PhieuDongPalletService phieuDongPalletService;

    @GetMapping
    public ApiResponse<List<PhieuDongPalletResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.layDanhSach(trangThai, principal.getName()), "OK");
    }

    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucDongPalletResponse> danhMuc() {
        return ApiResponse.success(phieuDongPalletService.layDanhMuc(), "OK");
    }

    @GetMapping("/du-kien")
    public ApiResponse<DuKienDongPalletResponse> duKien(@RequestParam Long dinhMucId, @RequestParam Long khoId,
                                                        @RequestParam int soLuong, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.duKien(dinhMucId, khoId, soLuong, principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<PhieuDongPalletResponse> layChiTiet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.layChiTiet(id, principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<PhieuDongPalletResponse> taoMoi(@Valid @RequestBody TaoPhieuDongPalletRequest req, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.taoMoi(req, principal.getName()), "Tạo phiếu đóng pallet thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<PhieuDongPalletResponse> capNhat(@PathVariable Long id,
                                                        @Valid @RequestBody TaoPhieuDongPalletRequest req, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.capNhat(id, req, principal.getName()), "Cập nhật phiếu thành công");
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<PhieuDongPalletResponse> duyet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.duyet(id, principal.getName()), "Đã duyệt phiếu đóng pallet");
    }

    @PostMapping("/{id}/huy")
    public ApiResponse<PhieuDongPalletResponse> huy(@PathVariable Long id,
                                                    @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(phieuDongPalletService.huy(id, lyDo, principal.getName()), "Đã hủy phiếu đóng pallet");
    }
}