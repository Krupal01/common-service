package com.krunish.common.security.generic;

/**
 * Marker interface representing the authenticated principal's claims
 * extracted from a validated JWT.
 *
 * Each service defines its own implementation with the fields it cares about:
 *
 *   // Minimal — just ID + email
 *   public record BasicAuthClaims(Long userId, String email) implements AuthClaims {}
 *
 *   // Rich — UUID + email + role + orgId + kycStatus + ...
 *   public record CrmAuthClaims(UUID userId, String email, String role, ...) implements AuthClaims {}
 *
 * The common-lib ships a DefaultAuthClaims that covers the most common case.
 */
public interface AuthClaims {
    // Intentionally empty — acts as a type-safe marker.
    // Services access fields directly on their concrete implementation.
}


