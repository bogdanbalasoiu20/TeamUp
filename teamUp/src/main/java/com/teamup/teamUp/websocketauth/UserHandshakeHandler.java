package com.teamup.teamUp.websocketauth;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Creates a Principal (username) for the WebSocket session.
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler implements HandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      org.springframework.web.socket.WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Get username saved by JwtHandshakeInterceptor
        String username = (String) attributes.get("ws-username");

        // Fallback if username is missing
        if (username == null) username = "anonymous";

        // Return a Principal with this username
        final String finalUsername = username;
        return () -> finalUsername;
    }
}