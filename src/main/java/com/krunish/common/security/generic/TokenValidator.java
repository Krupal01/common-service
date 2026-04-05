package com.krunish.common.security.generic;

/**
 * Contract for validating an incoming token string and returning
 * a typed {@link AuthClaims} implementation.
 *
 * common-lib provides {@link GenericJwtTokenValidator} as the default implementation.
 * Services can replace it entirely — e.g. for opaque tokens, API keys,
 * or a different JWT library — by registering their own bean.
 *
 * Example — custom validator:
 *
 *   @Bean
 *   public TokenValidator<CrmAuthClaims> tokenValidator(...) {
 *       return token -> { ... return new CrmAuthClaims(...); };
 *   }
 */
@FunctionalInterface
public interface TokenValidator<C extends AuthClaims> {
    /**
     * @param token  raw token string (e.g. value after "Bearer ")
     * @return typed claims on success
     * @throws RuntimeException (e.g. ExpiredJwtException) on failure — JwtFilter handles it
     */
    C validate(String token);
}