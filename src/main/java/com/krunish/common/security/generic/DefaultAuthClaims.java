package com.krunish.common.security.generic;

/**
 * Default AuthClaims implementation shipped with common-lib.
 *
 * Works out of the box for services that store a Long userId and email in JWT.
 * Legacy services that used AuthUser<Long> can switch to this with zero changes.
 *
 * Usage — no extra config needed:
 *   Long userId = AuthContext.<DefaultAuthClaims>getClaims().userId();
 *   String email = AuthContext.<DefaultAuthClaims>getClaims().email();
 */
public record DefaultAuthClaims(
        Long userId,
        String email
) implements AuthClaims {}
