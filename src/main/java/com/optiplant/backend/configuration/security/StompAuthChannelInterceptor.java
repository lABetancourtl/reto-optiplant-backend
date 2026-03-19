package com.optiplant.backend.configuration.security;

import java.security.Principal;
import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final JwtRoleConverter jwtRoleConverter;

    public StompAuthChannelInterceptor(JwtService jwtService, JwtRoleConverter jwtRoleConverter) {
        this.jwtService = jwtService;
        this.jwtRoleConverter = jwtRoleConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                throw new RuntimeException("Authorization header is required for WebSocket CONNECT");
            }

            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            if (!jwtService.isTokenValid(token)) {
                throw new RuntimeException("Invalid JWT token");
            }

            Claims claims = jwtService.extractClaims(token);
            String username = claims.getSubject();
            Collection<? extends GrantedAuthority> authorities = jwtRoleConverter.convert(claims);

            if (username == null || username.isBlank() || authorities == null || authorities.isEmpty()) {
                throw new RuntimeException("Invalid authentication data in JWT token");
            }

            Principal principal = new UsernamePasswordAuthenticationToken(username, null, authorities);
            accessor.setUser(principal);
        }

        return message;
    }
}

