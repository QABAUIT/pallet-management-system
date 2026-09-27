package com.pallet.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pallet.backend.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * QUAN TRỌNG: nếu không khai báo bean này, Spring Security mặc định fallback về
 * Http403ForbiddenEntryPoint khi app không có formLogin()/httpBasic() (đúng trường hợp
 * app JWT thuần của mình) - nghĩa là request CHƯA đăng nhập cũng bị trả 403 thay vì 401,
 * gây nhầm lẫn không phân biệt được "chưa đăng nhập" và "không đủ quyền".
 * Đây chính là nguyên nhân lỗi 403 khi bạn "skip đăng nhập" đã báo.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Chưa đăng nhập hoặc token không hợp lệ/đã hết hạn. Vui lòng đăng nhập lại.");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
