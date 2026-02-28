package com.krunish.common.event;

import com.krunish.common.security.OrgContext;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@MappedSuperclass
@Filter(name = "orgFilter", condition = "org_id = :orgId")
public abstract class BaseOrgEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long orgId;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        orgId = orgId == null ?
                OrgContext.getOrgId() : orgId;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
