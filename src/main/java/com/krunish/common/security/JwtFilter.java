package com.krunish.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final OrgAccessValidator orgAccessValidator;
    private final AuthProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String path = request.getRequestURI();

            if (properties.isPublic(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String token = authHeader.substring(7);

            // Validate JWT → returns user only
            AuthUser user = jwtValidator.validate(token);

            String orgHeader = request.getHeader("X-ORG-ID");

            if (orgHeader == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Long orgId = Long.parseLong(orgHeader);

            // Validate user belongs to org
            orgAccessValidator.validate(user.userId(), orgId);

            // Set context
            OrgContext.set(
                    user.userId(),
                    orgId,
                    user.email(),
                    false
            );

            filterChain.doFilter(request, response);
        } finally {
            OrgContext.clear();
        }
    }
}
