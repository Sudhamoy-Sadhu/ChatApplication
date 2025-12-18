package com.example.chat.Presence;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionRegistry {

    // userId -> (sessionId -> lastSeen)
    private final Map<Long, Map<String, Instant>> sessions = new ConcurrentHashMap<>();

    private static final long TIMEOUT_SECONDS = 40;

    public void addSession(Long userId, String sessionId) {
        sessions
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(sessionId, Instant.now());
    }

    public void removeSession(Long userId, String sessionId) {
        Map<String, Instant> userSessions = sessions.get(userId);
        if (userSessions == null) return;

        userSessions.remove(sessionId);
        if (userSessions.isEmpty()) {
            sessions.remove(userId);
        }
    }

    public void refresh(Long userId, String sessionId) {
        Map<String, Instant> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.put(sessionId, Instant.now());
        }
    }

    public boolean isOnline(Long userId) {
        Map<String, Instant> userSessions = sessions.get(userId);
        if (userSessions == null) return false;

        Instant now = Instant.now();

        userSessions.entrySet().removeIf(
            e -> now.minusSeconds(TIMEOUT_SECONDS).isAfter(e.getValue())
        );

        return !userSessions.isEmpty();
    }

    public Set<Long> getOnlineUsers() {
        return sessions.keySet();
    }
}
