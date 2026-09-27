package com.pallet.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pallet.backend.dto.request.LoginRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.LoginResponse;
import com.pallet.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response =
                authService.login(request, ip, userAgent);

        return ApiResponse.success(response, "Đăng nhập thành công");
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");

        return ApiResponse.success(
                authService.refreshToken(refreshToken),
                "Làm mới token thành công");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody Map<String, String> body) {

        authService.logout(body.get("refreshToken"));

        return ApiResponse.success(null, "Đăng xuất thành công");
    }
}