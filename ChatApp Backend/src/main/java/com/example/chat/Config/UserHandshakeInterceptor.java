package com.example.chat.Config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable; // <-- this one
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   @Nullable Map<String, Object> attributes) { // annotation added

        String query = request.getURI().getQuery();
        String username = "UnknownUser";

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("username=")) {
                    String[] parts = param.split("=", 2);
                    if (parts.length == 2 && !parts[1].isEmpty()) {
                        username = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    }
                    break;
                }
            }
        }

        if (attributes != null) { // since it's nullable
            attributes.put("username", username);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) { // annotation added
        // no operation
    }
}
