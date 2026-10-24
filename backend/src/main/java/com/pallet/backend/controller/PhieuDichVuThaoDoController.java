package com.pallet.backend.controller;

import com.pallet.backend.dto.request.KetQuaDichVuRequest;
import com.pallet.backend.dto.request.TiepNhanDichVuRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.PhieuDichVuThaoDoResponse;
import com.pallet.backend.dto.response.TonKhoKhachResponse;
import com.pallet.backend.service.PhieuDichVuThaoDoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dich-vu-thao-do")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class PhieuDichVuThaoDoController {

    private final PhieuDichVuThaoDoService service;

    @GetMapping
    public ApiResponse<List<PhieuDichVuThaoDoResponse>> layDanhSach(
            @RequestParam(required = false) String trangThai, Principal principal) {
        return ApiResponse.success(service.layDanhSach(trangThai, principal.getName()), "OK");
    }

    @GetMapping("/ton-khach")
    public ApiResponse<TonKhoKhachResponse> tonKhach(
            @RequestParam(required = false) Long khachHangId, Principal principal) {
        return ApiResponse.success(service.layTonKhach(khachHangId, principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<PhieuDichVuThaoDoResponse> layChiTiet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(service.layChiTiet(id, principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<PhieuDichVuThaoDoResponse> tiepNhan(@Valid @RequestBody TiepNhanDichVuRequest req, Principal principal) {
        return ApiResponse.success(service.tiepNhan(req, principal.getName()), "Đã tiếp nhận pallet của khách");
    }

    @PutMapping("/{id}/ket-qua")
    public ApiResponse<PhieuDichVuThaoDoResponse> ghiNhanKetQua(@PathVariable Long id,
                                                                @Valid @RequestBody KetQuaDichVuRequest req, Principal principal) {
        return ApiResponse.success(service.ghiNhanKetQua(id, req, principal.getName()), "Đã ghi nhận kết quả tháo dỡ, chờ duyệt");
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<PhieuDichVuThaoDoResponse> duyet(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(service.duyet(id, principal.getName()), "Đã duyệt kết quả tháo dỡ");
    }

    @PostMapping("/{id}/tra-hang")
    public ApiResponse<PhieuDichVuThaoDoResponse> traHang(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(service.traHang(id, principal.getName()), "Đã trả hàng cho khách");
    }

    @PostMapping("/{id}/huy")
    public ApiResponse<PhieuDichVuThaoDoResponse> huy(@PathVariable Long id,
                                                      @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(service.huy(id, lyDo, principal.getName()), "Đã hủy phiếu dịch vụ");
    }
}