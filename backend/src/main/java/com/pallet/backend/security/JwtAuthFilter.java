package com.pallet.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Đọc header "Authorization: Bearer <token>" mỗi request, xác thực và nạp thông tin
 * người dùng vào SecurityContext. Không tự chặn request ở đây (401 do SecurityConfig quyết định),
 * filter này CHỈ nạp thông tin nếu token hợp lệ, request không có token vẫn đi tiếp bình thường -
 * SecurityConfig sẽ quyết định endpoint đó có bắt buộc đăng nhập hay không.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            String tenDangNhap = jwtUtil.extractTenDangNhap(token);
            if (tenDangNhap != null && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtUtil.isTokenValid(token, tenDangNhap)) {

                String vaiTro = jwtUtil.extractVaiTro(token);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro));

                var authToken = new UsernamePasswordAuthenticationToken(tenDangNhap, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // Token sai/hết hạn -> không nạp authentication, request sẽ bị 401 nếu endpoint yêu cầu đăng nhập.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
