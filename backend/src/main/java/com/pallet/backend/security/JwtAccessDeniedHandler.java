package com.pallet.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pallet.backend.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Chỉ được gọi khi ĐÃ đăng nhập hợp lệ nhưng vai trò không đủ quyền cho route đó
 * (VD: NV bán hàng gọi /mat-hang/**). Trả JSON đồng bộ với ApiResponse thay vì
 * trang lỗi mặc định của servlet container.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "Tài khoản của bạn không có quyền truy cập chức năng này.");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
