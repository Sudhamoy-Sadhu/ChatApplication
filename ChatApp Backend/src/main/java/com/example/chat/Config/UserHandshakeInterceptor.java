package com.example.chat.Config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.chat.Service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            @Nullable Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest req = servletRequest.getServletRequest();
        String token = null;

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("access_token".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isBlank()) {
            // no token -> reject handshake (or you can allow anonymous connections if you
            // prefer)
            System.out.println("❌ NO JWT IN COOKIE → Reject WebSocket");
            return false;
        }

        if (token != null) {
            System.out.println("🔐 JWT FOUND in WebSocket handshake: " + token.substring(0, 20) + "...");
        }

        // extract subject (user id) using existing method
        String subject = jwtService.extractUsername(token);
        if (subject == null) {
            System.out.println("❌ Invalid JWT → Reject WebSocket");
            return false;
        }

        Long userId;
        try {
            userId = Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            System.out.println("❌ Invalid subject in JWT: " + subject);
            return false;
        }

        // ensure attributes map exists and store userId
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put("userId", userId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            @Nullable Exception exception) {
        // no-op
    }
}
