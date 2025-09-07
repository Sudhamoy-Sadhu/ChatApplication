package com.example.chat.Config;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        // Retrieve username stored by UserHandshakeInterceptor
        String username = (String) attributes.get("username");

        // Return Principal with this username
        return new Principal() {
            @Override
            public String getName() {
                return username;
            }
        };
    }
}
