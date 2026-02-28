package com.cluely.websocket.config;

import com.cluely.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT from headers
            List<String> authorization = accessor.getNativeHeader("Authorization");

            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);

                // Remove "Bearer " prefix if present
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                try {
                    // Validate token
                    if (jwtService.isValid(token)) {
                        String userId = jwtService.getUserId(token);

                        // Set user in WebSocket session
                        accessor.setUser(new UsernamePasswordAuthenticationToken(
                                userId, null, null));

                        log.info("WebSocket authenticated for user: {}", userId);
                    } else {
                        log.warn("Invalid JWT token in WebSocket connection");
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Authentication failed");
                }
            } else {
                log.warn("WebSocket connection without Authorization header");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}