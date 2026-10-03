package com.myeshopping.orderservice.event;

import com.myeshopping.orderservice.entity.Order;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("postgres")
public class OrderEventPublisher {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreated(Order order) {
        kafkaTemplate.send("order-events", String.valueOf(order.getId()),
                DomainEvent.orderCreated(order.getId(), order, currentCorrelationId()));
    }

    /** Reuse the HTTP request's trace ID so the whole fulfilment chain shares it. */
    private UUID currentCorrelationId() {
        String current = MDC.get("correlationId");
        if (current != null) {
            try {
                return UUID.fromString(current);
            } catch (IllegalArgumentException ignored) {
                // not a UUID: fall through to a fresh one
            }
        }
        return UUID.randomUUID();
    }
}
