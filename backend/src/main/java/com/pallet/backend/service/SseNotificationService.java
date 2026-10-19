package com.pallet.backend.service;

import com.pallet.backend.dto.response.ThongBaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseNotificationService {

    // Map: username -> Danh sách các SseEmitter của user đó (cho phép 1 user mở nhiều tab)
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // Timeout: 30 phút (sau đó browser tự động kết nối lại)
    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Đăng ký kết nối SSE cho một user
     */
    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        emitters.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError((e) -> removeEmitter(username, emitter));

        // Gửi event ping đầu tiên để báo client kết nối thành công
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("CONNECTED"));
        } catch (IOException e) {
            removeEmitter(username, emitter);
        }

        return emitter;
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(username);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(username);
            }
        }
    }

    /**
     * Gửi thông báo đến 1 user cụ thể
     */
    public void sendToUser(String username, ThongBaoResponse notification) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(username);
        if (userEmitters != null && !userEmitters.isEmpty()) {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("NOTIFICATION")
                            .data(notification));
                } catch (IOException e) {
                    removeEmitter(username, emitter);
                }
            }
        }
    }

    /**
     * Broadcast thông báo tới tất cả client đang kết nối (hoặc lọc theo role)
     */
    public void broadcast(ThongBaoResponse notification) {
        emitters.forEach((user, list) -> {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("NOTIFICATION")
                            .data(notification));
                } catch (IOException e) {
                    removeEmitter(user, emitter);
                }
            }
        });
    }
}
