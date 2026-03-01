package com.krunish.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private OrgAccessValidator orgAccessValidator;
    private final AuthProperties properties;

    // ✅ Constructor without OrgAccessValidator
    public JwtFilter(JwtValidator jwtValidator, AuthProperties properties) {
        this.jwtValidator = jwtValidator;
        this.properties = properties;
    }

    // ✅ Called only if service provides OrgAccessValidator
    public void setOrgAccessValidator(OrgAccessValidator orgAccessValidator) {
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
            System.out.println(">>> [JwtFilter] Incoming request: " + request.getMethod() + " " + path);
            System.out.println(">>> [JwtFilter] Public paths configured: " + properties.getPublicPaths());
            System.out.println(">>> [JwtFilter] Is public path: " + properties.isPublic(path));

            if (properties.isPublic(path)) {
                System.out.println(">>> [JwtFilter] ✅ Public path — skipping auth");
                filterChain.doFilter(request, response);
                return;
            }

            Long orgId = 0L;
            AuthUser user;

            String authHeader = request.getHeader("Authorization");
            System.out.println(">>> [JwtFilter] Authorization header: " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println(">>> [JwtFilter] ❌ Missing or invalid Authorization header → 401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String token = authHeader.substring(7);
            System.out.println(">>> [JwtFilter] Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

            // Validate JWT → returns user only
            try {
                user = jwtValidator.validate(token);
                System.out.println(">>> [JwtFilter] ✅ JWT valid — userId: " + user.userId() + ", email: " + user.email());
            } catch (Exception e) {
                System.out.println(">>> [JwtFilter] ❌ JWT validation failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }


            if(orgAccessValidator != null) {
                String orgHeader = request.getHeader("X-ORG-ID");
                System.out.println(">>> [JwtFilter] X-ORG-ID header: " + orgHeader);

                if (orgHeader == null) {
                    System.out.println(">>> [JwtFilter] ❌ Missing X-ORG-ID header → 400");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                try {
                    orgId = Long.parseLong(orgHeader);
                    System.out.println(">>> [JwtFilter] Parsed orgId: " + orgId);
                } catch (NumberFormatException e) {
                    System.out.println(">>> [JwtFilter] ❌ Invalid X-ORG-ID format: " + orgHeader + " → 400");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                // Validate user belongs to org
                try {
                    orgAccessValidator.validate(user.userId(), orgId);
                    System.out.println(">>> [JwtFilter] ✅ Org access valid for userId: " + user.userId() + ", orgId: " + orgId);
                } catch (Exception e) {
                    System.out.println(">>> [JwtFilter] ❌ Org access validation failed: " + e.getMessage());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }


            // Set context
            OrgContext.set(
                    user.userId(),
                    orgId,
                    user.email(),
                    false
            );
            System.out.println(">>> [JwtFilter] ✅ OrgContext set — userId: " + user.userId() + ", orgId: " + orgId + ", email: " + user.email());

            // ── Step 7: Continue filter chain ─────────────────────
            System.out.println(">>> [JwtFilter] ✅ Auth complete — proceeding to controller");
            System.out.println("==============================");


            filterChain.doFilter(request, response);
        } finally {
            OrgContext.clear();
            System.out.println(">>> [JwtFilter] OrgContext cleared");
        }
    }
}
