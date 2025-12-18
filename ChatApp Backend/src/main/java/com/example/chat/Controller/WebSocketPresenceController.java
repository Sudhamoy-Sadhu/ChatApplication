package com.example.chat.Controller;

import com.example.chat.Presence.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketPresenceController {

    private final UserSessionRegistry registry;

    @MessageMapping("/heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor acc) {

        if (acc.getUser() == null)
            return;

        Long userId = Long.valueOf(acc.getUser().getName());
        String sessionId = acc.getSessionId();

        registry.refresh(userId, sessionId);
    }
}
