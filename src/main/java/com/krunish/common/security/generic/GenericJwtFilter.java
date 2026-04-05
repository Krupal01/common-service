package com.krunish.common.security.generic;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import com.krunish.common.security.AuthProperties;

import java.io.IOException;

/**
 * Generic JWT filter.
 *
 * Delegates token validation to {@link TokenValidator} (pluggable).
 * Stores the resulting {@link AuthClaims} in {@link AuthContext} for the request lifetime.
 * Clears AuthContext in the finally block — no leaks between requests.
 *
 * OrgAccessValidator is optional — only wired when the service needs org-level access control.
 */
public class GenericJwtFilter<C extends AuthClaims> extends OncePerRequestFilter implements AuthFilter {

    private final TokenValidator<C> tokenValidator;
    private final AuthProperties properties;
    private GenericOrgAccessValidator orgAccessValidator; // optional

    public GenericJwtFilter(TokenValidator<C> tokenValidator, AuthProperties properties) {
        this.tokenValidator = tokenValidator;
        this.properties = properties;
    }

    public void setOrgAccessValidator(GenericOrgAccessValidator orgAccessValidator) {
        this.orgAccessValidator = orgAccessValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            System.out.println("==============================");
            System.out.println(">>> [JwtFilter] " + request.getMethod() + " " + path);

            if (properties.isPublic(path)) {
                System.out.println(">>> [JwtFilter] ✅ Public path — skipping auth");
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println(">>> [JwtFilter] ❌ Missing/invalid Authorization header → 401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String token = authHeader.substring(7);
            C claims;

            try {
                claims = tokenValidator.validate(token);
                System.out.println(">>> [JwtFilter] ✅ Token valid — claims: " + claims);
            } catch (Exception e) {
                System.out.println(">>> [JwtFilter] ❌ Token validation failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long orgId = 0L;

            if (orgAccessValidator != null) {
                String orgHeader = request.getHeader("X-ORG-ID");

                if (orgHeader == null) {
                    System.out.println(">>> [JwtFilter] ❌ Missing X-ORG-ID header → 400");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                try {
                    orgId = Long.parseLong(orgHeader);
                } catch (NumberFormatException e) {
                    System.out.println(">>> [JwtFilter] ❌ Invalid X-ORG-ID: " + orgHeader + " → 400");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                try {
                    orgAccessValidator.validate(claims, orgId);
                    System.out.println(">>> [JwtFilter] ✅ Org access valid — orgId: " + orgId);
                } catch (Exception e) {
                    System.out.println(">>> [JwtFilter] ❌ Org access denied: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            AuthContext.set(claims, orgId, false);
            System.out.println(">>> [JwtFilter] ✅ AuthContext set — proceeding to controller");
            System.out.println("==============================");

            filterChain.doFilter(request, response);

        } finally {
            AuthContext.clear();
        }
    }
}
