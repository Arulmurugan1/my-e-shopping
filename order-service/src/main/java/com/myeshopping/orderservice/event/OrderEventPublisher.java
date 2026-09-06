package com.myeshopping.orderservice.event;

import com.myeshopping.orderservice.entity.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class OrderEventPublisher {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreated(Order order) {
        kafkaTemplate.send("order-events", String.valueOf(order.getId()),
                DomainEvent.orderCreated(order.getId(), order));
    }
}
