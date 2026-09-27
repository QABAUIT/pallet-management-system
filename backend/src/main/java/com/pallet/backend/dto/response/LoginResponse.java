package com.pallet.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long nhanVienId;
    private String hoTen;
    private String maVaiTro;
    private long accessTokenExpiresInMinutes;
}
