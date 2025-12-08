package com.example.chat.Config;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Interceptor that reads userId from WebSocket session attributes (set during handshake)
 * and sets a Principal for STOMP messages.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // session attributes come from HandshakeInterceptor
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            if (sessionAttrs == null) {
                // reject or leave anonymous
                return message;
            }

            Object uid = sessionAttrs.get("userId");
            if (uid instanceof Number || uid instanceof String) {
                final String principalName = String.valueOf(uid);
                // simple Principal implementation
                Principal user = () -> principalName;
                accessor.setUser(user);
            }
        }

        return message;
    }
}
