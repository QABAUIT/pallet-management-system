package com.pallet.backend.service;

import com.pallet.backend.dto.request.LoginRequest;
import com.pallet.backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
