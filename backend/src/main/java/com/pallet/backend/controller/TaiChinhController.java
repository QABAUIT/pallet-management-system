package com.pallet.backend.controller;

import com.pallet.backend.dto.request.ChiPhiVanHanhRequest;
import com.pallet.backend.dto.request.MucTieuDoanhThuRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.BaoCaoDoanhThuResponse;
import com.pallet.backend.dto.response.BaoCaoLoiNhuanResponse;
import com.pallet.backend.dto.response.BaoCaoThueResponse;
import com.pallet.backend.dto.response.ChiPhiVanHanhResponse;
import com.pallet.backend.dto.response.MucTieuDoanhThuResponse;
import com.pallet.backend.service.TaiChinhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/** Xem: GD, PGD, NVBANHANG. Ghi (chi phí, mục tiêu): chỉ GD, PGD. */
@RestController
@RequestMapping("/api/v1/tai-chinh")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVBANHANG')")
public class TaiChinhController {

    private final TaiChinhService taiChinhService;

    @GetMapping("/doanh-thu")
    public ApiResponse<BaoCaoDoanhThuResponse> doanhThu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ApiResponse.success(taiChinhService.baoCaoDoanhThu(tuNgay, denNgay), "OK");
    }

    @GetMapping("/thue")
    public ApiResponse<BaoCaoThueResponse> thue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ApiResponse.success(taiChinhService.baoCaoThue(tuNgay, denNgay), "OK");
    }

    @GetMapping("/loi-nhuan")
    public ApiResponse<BaoCaoLoiNhuanResponse> loiNhuan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ApiResponse.success(taiChinhService.baoCaoLoiNhuan(tuNgay, denNgay), "OK");
    }

    // ----- Chi phí vận hành -----

    @GetMapping("/chi-phi")
    public ApiResponse<List<ChiPhiVanHanhResponse>> layChiPhi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ApiResponse.success(taiChinhService.layChiPhi(tuNgay, denNgay), "OK");
    }

    @PostMapping("/chi-phi")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<ChiPhiVanHanhResponse> taoChiPhi(@Valid @RequestBody ChiPhiVanHanhRequest req,
                                                        Principal principal) {
        return ApiResponse.success(taiChinhService.taoChiPhi(req, principal.getName()), "Đã thêm khoản chi");
    }

    @PutMapping("/chi-phi/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<ChiPhiVanHanhResponse> suaChiPhi(@PathVariable Long id,
                                                        @Valid @RequestBody ChiPhiVanHanhRequest req,
                                                        Principal principal) {
        return ApiResponse.success(taiChinhService.suaChiPhi(id, req, principal.getName()), "Đã cập nhật khoản chi");
    }

    @DeleteMapping("/chi-phi/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<Void> xoaChiPhi(@PathVariable Long id) {
        taiChinhService.xoaChiPhi(id);
        return ApiResponse.success(null, "Đã xóa khoản chi");
    }

    // ----- Mục tiêu doanh thu -----

    @GetMapping("/muc-tieu")
    public ApiResponse<List<MucTieuDoanhThuResponse>> layMucTieu(@RequestParam Integer nam) {
        return ApiResponse.success(taiChinhService.layMucTieu(nam), "OK");
    }

    @PostMapping("/muc-tieu")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<MucTieuDoanhThuResponse> datMucTieu(@Valid @RequestBody MucTieuDoanhThuRequest req,
                                                           Principal principal) {
        return ApiResponse.success(taiChinhService.datMucTieu(req, principal.getName()), "Đã lưu mục tiêu doanh thu");
    }

    @DeleteMapping("/muc-tieu/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<Void> xoaMucTieu(@PathVariable Long id) {
        taiChinhService.xoaMucTieu(id);
        return ApiResponse.success(null, "Đã xóa mục tiêu");
    }
}