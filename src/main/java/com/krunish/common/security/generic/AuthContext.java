package com.krunish.common.security.generic;

/**
 * Thread-local holder for the full authenticated request context.
 *
 * Replaces the old OrgContext. Stores:
 *   - typed AuthClaims (userId, email, role, kycStatus, etc.)
 *   - orgId           (0L if the service doesn't use org filtering)
 *   - isServiceToken  (true if request came via X-SERVICE-TOKEN)
 *
 * Usage in a controller or service:
 *
 *   // CRM service
 *   CrmAuthClaims claims = AuthContext.getClaims();
 *   UUID userId  = claims.userId();
 *   String role  = claims.role();
 *
 *   // Legacy service
 *   DefaultAuthClaims claims = AuthContext.getClaims();
 *   Long userId = claims.userId();
 *
 * AuthContext is set by JwtFilter and cleared in its finally block —
 * callers never need to manage lifecycle.
 */
public final class AuthContext {

    private static final ThreadLocal<AuthContext> HOLDER = new ThreadLocal<>();

    private final AuthClaims claims;
    private final Long orgId;
    private final boolean isServiceToken;

    private AuthContext(AuthClaims claims, Long orgId, boolean isServiceToken) {
        this.claims = claims;
        this.orgId = orgId;
        this.isServiceToken = isServiceToken;
    }

    // ── Write (called only by JwtFilter / ServiceTokenFilter) ────────────────

    public static void set(AuthClaims claims, Long orgId, boolean isServiceToken) {
        if (HOLDER.get() != null) {
            // warn instead of silently overwriting
            System.out.println(">>> [AuthContext] ⚠️ WARNING — AuthContext already set for this request, overwriting");
        }
        HOLDER.set(new AuthContext(claims, orgId, isServiceToken));
    }

    public static void set(AuthClaims claims) {
        if (HOLDER.get() != null) {
            // warn instead of silently overwriting
            System.out.println(">>> [AuthContext] ⚠️ WARNING — AuthContext already set for this request, overwriting");
        }
        set(claims, 0L, false);
    }

    public static void clear() {
        HOLDER.remove();
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Returns the typed claims.
     * Cast is safe because JwtFilter<C> always sets claims of type C.
     *
     *   CrmAuthClaims claims = AuthContext.getClaims();
     */
    @SuppressWarnings("unchecked")
    public static <C extends AuthClaims> C getClaims() {
        return (C) current().claims;
    }

    public static Long getOrgId() {
        return current().orgId;
    }

    public static boolean isServiceToken() {
        return current().isServiceToken;
    }

    // ── Convenience shortcuts (avoids casting in simple cases) ────────────────

    /**
     * Only usable when claims implement a known interface or record.
     * Prefer getClaims() and access fields directly on the concrete type.
     */
    public static AuthClaims getRawClaims() {
        return current().claims;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private static AuthContext current() {
        AuthContext ctx = HOLDER.get();
        if (ctx == null) {
            throw new IllegalStateException(
                    "AuthContext is not set — must be called within an authenticated request"
            );
        }
        return ctx;
    }
}