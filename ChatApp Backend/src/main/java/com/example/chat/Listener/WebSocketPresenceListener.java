package com.example.chat.Listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.chat.Presence.UserSessionRegistry;
import com.example.chat.Service.MessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceListener {

    private final UserSessionRegistry registry;
    private final MessageService messageService;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {

        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        if (acc.getUser() == null)
            return;

        Long userId = Long.valueOf(acc.getUser().getName());
        String sessionId = acc.getSessionId();

        registry.addSession(userId, sessionId);

        messageService.deliverPendingMessages(userId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());

        if (acc.getUser() == null)
            return;

        String userIdStr = acc.getUser().getName();
        String sessionId = acc.getSessionId();

        registry.removeSession(Long.valueOf(userIdStr), sessionId);
    }
}
