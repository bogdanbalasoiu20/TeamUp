package com.teamup.teamUp.config;

import com.teamup.teamUp.security.JwtService;
import com.teamup.teamUp.websocketauth.JwtHandshakeInterceptor;
import com.teamup.teamUp.websocketauth.UserHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket handshake endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(new JwtHandshakeInterceptor(jwtService)) // validate JWT
                .setHandshakeHandler(new UserHandshakeHandler())          // create Principal
                .setAllowedOriginPatterns("*");                           // allow all origins (dev only)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages sent from client to server
        registry.setApplicationDestinationPrefixes("/app");

        // Simple in-memory message broker for broadcasts
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for private user messages (not used yet)
        registry.setUserDestinationPrefix("/user");
    }
}