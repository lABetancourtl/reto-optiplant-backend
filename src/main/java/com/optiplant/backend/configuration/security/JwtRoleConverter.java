package com.optiplant.backend.configuration.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

@Component
public class JwtRoleConverter implements Converter<Claims, Collection<? extends GrantedAuthority>> {

    @Override
    public Collection<? extends GrantedAuthority> convert(Claims claims) {
        String role = claims.get("role", String.class);

        if (role == null || role.trim().isEmpty()) {
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List<?> roles && !roles.isEmpty() && roles.get(0) != null) {
                role = String.valueOf(roles.get(0));
            }
        }

        if (role == null || role.trim().isEmpty()) {
            return List.of();
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring(5);
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
    }
}

