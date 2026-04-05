package com.krunish.common.security.generic;

import com.krunish.common.security.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

/**
 * Default JWT implementation of {@link TokenValidator}.
 *
 * Uses JJWT to verify the signature and delegates claim mapping
 * to a {@link ClaimsExtractor} provided by the service.
 *
 * Auto-registered by CommonAutoConfiguration via @ConditionalOnMissingBean(TokenValidator.class).
 * Services that need a completely different validation strategy can replace this
 * by registering their own TokenValidator bean.
 */
@RequiredArgsConstructor
public class GenericJwtTokenValidator<C extends AuthClaims> implements TokenValidator<C>, InitializingBean {

    private final AuthProperties properties;
    private final ClaimsExtractor<C> claimsExtractor;

    @PostConstruct
    public void init() {
        System.out.println(">>> [JwtTokenValidator] ✅ Bean initialized");
        System.out.println(">>> [JwtTokenValidator] Secret length = " +
                (properties.getSecurity().getSecret() != null
                        ? properties.getSecurity().getSecret().length()
                        : "NULL ❌"));
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(">>> [JwtTokenValidator] ✅ Bean initialized");
        System.out.println(">>> [JwtTokenValidator] Secret length = " +
                (properties.getSecurity().getSecret() != null
                        ? properties.getSecurity().getSecret().length()
                        : "NULL ❌"));
    }


    @Override
    public C validate(String token) {
        System.out.println(">>> [JwtTokenValidator] Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(properties.getSecurity().getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            C authClaims = claimsExtractor.extract(claims);
            System.out.println(">>> [JwtTokenValidator] ✅ Token valid — claims: " + authClaims);
            return authClaims;

        } catch (ExpiredJwtException e) {
            System.out.println(">>> [JwtTokenValidator] ❌ Token EXPIRED: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println(">>> [JwtTokenValidator] ❌ Token MALFORMED: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.out.println(">>> [JwtTokenValidator] ❌ Signature INVALID: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println(">>> [JwtTokenValidator] ❌ Validation FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
}
