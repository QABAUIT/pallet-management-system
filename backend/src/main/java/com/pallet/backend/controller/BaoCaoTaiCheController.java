package com.pallet.backend.controller;

import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.service.BaoCaoTaiCheService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bao-cao-tai-che")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD')")
public class BaoCaoTaiCheController {

    private final BaoCaoTaiCheService baoCaoService;

    @GetMapping("/hieu-suat")
    public ApiResponse<Map<String, Object>> hieuSuat(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
            @RequestParam(required = false) Long khoId) {
        return ApiResponse.success(baoCaoService.hieuSuat(tu, den, khoId), "OK");
    }

    @GetMapping("/theo-lo")
    public ApiResponse<List<Map<String, Object>>> theoLo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
            @RequestParam(required = false) Long khoId) {
        return ApiResponse.success(baoCaoService.theoLo(tu, den, khoId), "OK");
    }

    @GetMapping("/chenh-lech-dinh-muc")
    public ApiResponse<Map<String, Object>> chenhLech(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
            @RequestParam(required = false) Long khoId) {
        return ApiResponse.success(baoCaoService.chenhLechDinhMuc(tu, den, khoId), "OK");
    }
}