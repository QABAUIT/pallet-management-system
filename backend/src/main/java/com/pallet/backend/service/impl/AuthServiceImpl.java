package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.LoginRequest;
import com.pallet.backend.dto.response.LoginResponse;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhienDangNhap;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhienDangNhapRepository;
import com.pallet.backend.security.JwtUtil;
import com.pallet.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final NhanVienRepository nhanVienRepository;
    private final PhienDangNhapRepository phienDangNhapRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${app.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        NhanVien nv = nhanVienRepository.findByTenDangNhap(request.getTenDangNhap())
                .orElseThrow(() -> new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getMatKhau(), nv.getMatKhauHash())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Đúng theo quy tắc đã thống nhất: nhân viên "da_nghi_viec" không được đăng nhập.
        // "cong_tac" (đi công tác) vẫn được đăng nhập bình thường, chỉ "da_nghi_viec" mới chặn.
        if ("da_nghi_viec".equals(nv.getTrangThai())) {
            throw new BusinessException("Tài khoản đã nghỉ việc, không thể đăng nhập");
        }

        String accessToken = jwtUtil.generateAccessToken(
                nv.getTenDangNhap(), nv.getId(), nv.getVaiTro().getMaVaiTro());

        String refreshToken = UUID.randomUUID().toString();
        long expireDays = request.isGhiNhoDangNhap() ? refreshTokenExpirationDays : 1;

        PhienDangNhap phien = PhienDangNhap.builder()
                .nhanVien(nv)
                .refreshToken(refreshToken)
                .ghiNhoDangNhap(request.isGhiNhoDangNhap())
                .thietBi(userAgent)
                .ipAddress(ipAddress)
                .thoiGianTao(LocalDateTime.now())
                .thoiGianHetHan(LocalDateTime.now().plusDays(expireDays))
                .build();
        phienDangNhapRepository.save(phien);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nhanVienId(nv.getId())
                .hoTen(nv.getHoTen())
                .maVaiTro(nv.getVaiTro().getMaVaiTro())
                .accessTokenExpiresInMinutes(accessTokenExpirationMinutes)
                .build();
    }

    @Override
        @Transactional(noRollbackFor = BusinessException.class)
        public LoginResponse refreshToken(String refreshToken) {
        PhienDangNhap phien = phienDangNhapRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại"));

        if (phien.getThoiGianHetHan().isBefore(LocalDateTime.now())) {
                phienDangNhapRepository.delete(phien);
                throw new BusinessException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        }

        NhanVien nv = phien.getNhanVien();

        if ("da_nghi_viec".equals(nv.getTrangThai())) {
                phienDangNhapRepository.delete(phien);
                throw new BusinessException("Tài khoản đã nghỉ việc, không thể đăng nhập");
        }
        String newAccessToken = jwtUtil.generateAccessToken(
                nv.getTenDangNhap(), nv.getId(), nv.getVaiTro().getMaVaiTro());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .nhanVienId(nv.getId())
                .hoTen(nv.getHoTen())
                .maVaiTro(nv.getVaiTro().getMaVaiTro())
                .accessTokenExpiresInMinutes(accessTokenExpirationMinutes)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        phienDangNhapRepository.findByRefreshToken(refreshToken)
                .ifPresent(phienDangNhapRepository::delete);
    }
}
