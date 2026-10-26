package com.pallet.backend.controller;

import com.pallet.backend.dto.request.BanPheLieuRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucBanPheLieuResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.service.BanPheLieuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/ban-phe-lieu")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVBANHANG')")
public class BanPheLieuController {

    private final BanPheLieuService banPheLieuService;

    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucBanPheLieuResponse> danhMuc(Principal principal) {
        return ApiResponse.success(banPheLieuService.layDanhMuc(principal.getName()), "OK");
    }

    @PostMapping
    public ApiResponse<TaoHoaDonResponse> ban(@Valid @RequestBody BanPheLieuRequest req, Principal principal) {
        return ApiResponse.success(banPheLieuService.ban(req, principal.getName()), "Đã lập hóa đơn bán phế liệu");
    }
}