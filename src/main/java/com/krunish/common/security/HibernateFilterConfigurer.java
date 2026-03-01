package com.krunish.common.security;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class HibernateFilterConfigurer extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Long orgId = OrgContext.getOrgId(); // ✅ Read from ThreadLocal set by JwtFilter

        if (orgId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("orgFilter").setParameter("orgId", orgId); // ✅ Apply to ALL queries
        }

        filterChain.doFilter(request, response);
    }
}
