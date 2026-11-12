package com.pallet.backend.service;

import com.pallet.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn số lần đăng nhập sai theo cặp (tên đăng nhập + IP).
 * Lưu in-memory - đủ cho 1 instance. Nếu chạy nhiều instance cần thay bằng Redis.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record Attempt(int count, Instant firstAttempt, Instant blockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil() == null) {
            return;
        }
        if (attempt.blockedUntil().isAfter(Instant.now())) {
            long conLai = Math.max(1, Duration.between(Instant.now(), attempt.blockedUntil()).toMinutes());
            throw new BusinessException("Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau " + conLai + " phút.");
        }
        attempts.remove(key);
    }

    public void loginFailed(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, cu) -> {
            if (cu == null || cu.blockedUntil() != null || cu.firstAttempt().plus(WINDOW).isBefore(now)) {
                return new Attempt(1, now, null);
            }
            int count = cu.count() + 1;
            return count >= MAX_ATTEMPTS
                    ? new Attempt(count, cu.firstAttempt(), now.plus(LOCK_TIME))
                    : new Attempt(count, cu.firstAttempt(), null);
        });
    }

    public void loginSucceeded(String key) {
        attempts.remove(key);
    }
}
