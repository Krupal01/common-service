package com.krunish.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final AuthProperties properties;

    @PostConstruct
    public void init() {
        System.out.println(">>> [JwtValidator] ✅ Bean initialized");
        System.out.println(">>> [JwtValidator] Security config: secret length = " +
                (properties.getSecurity().getSecret() != null
                        ? properties.getSecurity().getSecret().length()
                        : "NULL ❌"));
    }

    public AuthUser validate(String token) {
        System.out.println(">>> [JwtValidator] Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");



        try {
            Claims claims = Jwts.parser()
                    .verifyWith(properties.getSecurity().getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            AuthUser user = new AuthUser(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class)
            );

            System.out.println(">>> [JwtValidator] ✅ Token valid — userId: " + user.userId() + ", email: " + user.email());
            return user;

        } catch (ExpiredJwtException e) {
            System.out.println(">>> [JwtValidator] ❌ Token EXPIRED: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println(">>> [JwtValidator] ❌ Token MALFORMED: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.out.println(">>> [JwtValidator] ❌ Token SIGNATURE INVALID — secret key mismatch?: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println(">>> [JwtValidator] ❌ Token validation FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
}
