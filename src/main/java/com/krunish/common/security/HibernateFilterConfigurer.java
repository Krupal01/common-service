package com.krunish.common.security;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HibernateFilterConfigurer {

    private final EntityManager entityManager;

    public HibernateFilterConfigurer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void enableFilter() {
        entityManager.unwrap(org.hibernate.Session.class)
                .enableFilter("orgFilter")
                .setParameter("orgId", OrgContext.getOrgId());
    }
}
