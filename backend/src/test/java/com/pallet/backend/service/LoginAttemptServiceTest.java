package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void choPhepKhiChuaSaiLanNao() {
        assertDoesNotThrow(() -> service.checkBlocked("user|127.0.0.1"));
    }

    @Test
    void khoaSau5LanSaiLienTiep() {
        String key = "user|127.0.0.1";
        for (int i = 0; i < 4; i++) {
            service.loginFailed(key);
            assertDoesNotThrow(() -> service.checkBlocked(key),
                    "Lần sai thứ " + (i + 1) + " chưa được khóa");
        }

        service.loginFailed(key);
        assertThrows(BusinessException.class, () -> service.checkBlocked(key),
                "Lần sai thứ 5 phải khóa tài khoản");
    }

    @Test
    void dangNhapThanhCongXoaLichSuSai() {
        String key = "user|127.0.0.1";
        for (int i = 0; i < 4; i++) {
            service.loginFailed(key);
        }

        service.loginSucceeded(key);
        for (int i = 0; i < 4; i++) {
            service.loginFailed(key);
            assertDoesNotThrow(() -> service.checkBlocked(key),
                    "Sau khi reset, 4 lần sai đầu tiên không được khóa");
        }
    }

    @Test
    void khoaDocLapTheoUsernameVaIP() {
        String key = "user|127.0.0.1";
        for (int i = 0; i < 5; i++) {
            service.loginFailed(key);
        }

        assertThrows(BusinessException.class, () -> service.checkBlocked(key));
        assertDoesNotThrow(() -> service.checkBlocked("user|10.0.0.2"));
        assertDoesNotThrow(() -> service.checkBlocked("other|127.0.0.1"));
    }
}
