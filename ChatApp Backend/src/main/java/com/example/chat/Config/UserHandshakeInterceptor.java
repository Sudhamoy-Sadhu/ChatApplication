package com.example.chat.Config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.chat.Service.JwtService;

@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   @Nullable Map<String, Object> attributes) {

        String query = request.getURI().getQuery();
        String username = "Anonymous";
        String token = null;

        if (query != null) {
            // simple parse token=... from query string
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring("token=".length());
                    // token may be URL-encoded
                    token = java.net.URLDecoder.decode(token, StandardCharsets.UTF_8);
                    break;
                }
            }
        }

        if (token != null && jwtService.validateAccessToken(token)) {
            String sub = jwtService.extractUsername(token);
            if (sub != null) {
                // use subject as principal name. Convert or map ID->username if needed.
                username = sub;
            }
        } else {
            // token missing or invalid -> keep Anonymous (dev behavior)
            // If you want to refuse handshake for invalid token, return false here.
        }

        if (attributes != null) {
            attributes.put("username", username);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
    }
}
