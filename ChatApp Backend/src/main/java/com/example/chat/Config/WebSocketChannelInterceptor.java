package com.example.chat.Config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;


import com.example.chat.Presence.UserSessionRegistry;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final UserSessionRegistry registry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getUser() != null) {
            Long userId = Long.valueOf(accessor.getUser().getName());
            String sessionId = accessor.getSessionId();
            registry.refresh(userId, sessionId);
        }
        return message;
    }
}
