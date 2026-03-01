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

        Long orgId = OrgContext.getOrgId();
        System.out.println(">>> [HibernateFilterConfigurer] Request: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println(">>> [HibernateFilterConfigurer] OrgId from OrgContext: " + orgId);

        if (orgId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("orgFilter").setParameter("orgId", orgId);
                System.out.println(">>> [HibernateFilterConfigurer] ✅ orgFilter enabled with orgId: " + orgId);
            } catch (Exception e) {
                System.out.println(">>> [HibernateFilterConfigurer] ❌ Failed to enable orgFilter: " + e.getMessage());
            }
        } else {
            System.out.println(">>> [HibernateFilterConfigurer] ⚠️ orgId is null — orgFilter NOT applied (public route or missing context)");
        }

        filterChain.doFilter(request, response);
    }
}
