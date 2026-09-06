package com.myeshopping.orderservice.event;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        Instant timestamp,
        UUID correlationId,
        String aggregateId,
        int version,
        Object payload) {

    public static DomainEvent orderCreated(Long orderId, Object payload) {
        return new DomainEvent(UUID.randomUUID(), "OrderCreated", Instant.now(), UUID.randomUUID(),
                "order-" + orderId, 1, payload);
    }
}
