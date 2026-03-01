package com.krunish.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final AuthProperties properties;

    public AuthUser validate(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(properties.getSecurity().getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthUser(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class)
        );
    }
}
