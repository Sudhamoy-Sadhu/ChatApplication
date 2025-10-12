package com.example.chat.Config;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.chat.Service.JwtService;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public StompAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null && accessor.getCommand().getMessageType().toString().equals("CONNECT")) {
            // Try Authorization header first
            List<String> auth = accessor.getNativeHeader("Authorization");
            String token = null;
            if (auth != null && !auth.isEmpty()) {
                String a = auth.get(0);
                if (a.startsWith("Bearer ")) token = a.substring(7);
            }

            // fallback: access_token header
            if (token == null) {
                List<String> at = accessor.getNativeHeader("access_token");
                if (at != null && !at.isEmpty()) token = at.get(0);
            }

            if (token != null && jwtService.validateAccessToken(token)) {
                String sub = jwtService.extractUsername(token);
                if (sub != null) {
                    Principal p = () -> sub;
                    accessor.setUser(p);
                }
            } else {
                // invalid/missing token -> keep anonymous or reject connection
                // To reject, return null here.
            }
        }

        return message;
    }
}
