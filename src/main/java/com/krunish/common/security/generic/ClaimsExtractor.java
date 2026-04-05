package com.krunish.common.security.generic;

import io.jsonwebtoken.Claims;

/**
 * Strategy interface for mapping raw JJWT {@link Claims} to a
 * service-specific {@link AuthClaims} implementation.
 *
 * Each service provides exactly ONE bean of this type.
 * common-lib registers a {@link DefaultClaimsExtractor} automatically
 * if no custom bean is found.
 *
 * Example — CRM service:
 *
 *   @Bean
 *   public ClaimsExtractor<CrmAuthClaims> claimsExtractor() {
 *       return claims -> new CrmAuthClaims(
 *           UUID.fromString(claims.getSubject()),
 *           claims.get("email", String.class),
 *           claims.get("role", String.class),
 *           claims.get("globalStatus", String.class),
 *           claims.get("kycStatus", String.class)
 *       );
 *   }
 *
 * Example — Legacy Long service (or just rely on DefaultClaimsExtractor):
 *
 *   @Bean
 *   public ClaimsExtractor<DefaultAuthClaims> claimsExtractor() {
 *       return claims -> new DefaultAuthClaims(
 *           Long.parseLong(claims.getSubject()),
 *           claims.get("email", String.class)
 *       );
 *   }
 */
@FunctionalInterface
public interface ClaimsExtractor<C extends AuthClaims> {
    C extract(Claims claims);
}
