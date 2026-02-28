package com.krunish.common.event;

import java.time.Instant;
import java.util.UUID;

public class KafkaEventEnvelope<T> {

    private UUID eventId;
    private String eventType;
    private String service;
    private Instant occurredAt;
    private UUID actorUserId;
    private UUID orgId;
    private T payload;

    public KafkaEventEnvelope(String eventType, String service, T payload) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.service = service;
        this.occurredAt = Instant.now();
        this.payload = payload;
    }
}
