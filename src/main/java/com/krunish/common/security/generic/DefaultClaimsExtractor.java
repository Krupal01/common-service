package com.krunish.common.security.generic;

import io.jsonwebtoken.Claims;

/**
 * Default ClaimsExtractor used when the service does not provide a custom one.
 *
 * Reads "sub" as a Long and "email" from the JWT.
 * Suitable for all legacy services that stored userId as Long.
 *
 * Auto-registered by CommonAutoConfiguration via @ConditionalOnMissingBean.
 */
public class DefaultClaimsExtractor implements ClaimsExtractor<DefaultAuthClaims> {

    @Override
    public DefaultAuthClaims extract(Claims claims) {
        return new DefaultAuthClaims(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class)
        );
    }
}
