package com.myeshopping.orderservice.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.orderservice.client.FulfillmentClient;
import com.myeshopping.orderservice.entity.Order;
import com.myeshopping.orderservice.entity.OrderLine;
import com.myeshopping.orderservice.entity.OrderStatus;
import com.myeshopping.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes OrderCreated events and drives the order through fulfilment:
 * order-group (PICKING_IN_PROGRESS) -> picking (PICKED) -> shipment (SHIPPED) -> notification (Kafka event).
 * Each step is chosen from the order's current status, so a retry resumes where the last attempt stopped.
 */
@Component
@Profile("postgres")
public class OrderFulfillmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfillmentConsumer.class);
    private static final String DEFAULT_CARRIER = "MYE-Express";
    private static final int NOTIFY_ATTEMPTS = 3;

    private final OrderService orderService;
    private final FulfillmentClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderFulfillmentConsumer(OrderService orderService, FulfillmentClient client) {
        this.orderService = orderService;
        this.client = client;
    }

    @KafkaListener(topics = "order-events", groupId = "order-fulfillment")
    public void onOrderEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        if (!"OrderCreated".equals(event.path("eventType").asText())) {
            return;
        }
        long orderId = event.path("payload").path("id").asLong();
        String correlationId = event.path("correlationId").asText(null);
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
            MDC.put("traceId", correlationId);
        }
        try {
            log.info("Fulfilment started for order {}", orderId);
            fulfil(orderId);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("traceId");
        }
    }

    private void fulfil(long orderId) {
        Order order = orderService.getOrder(orderId);
        OrderStatus status = order.getStatus();

        if (status == OrderStatus.ORDERED || status == OrderStatus.PICKING_PENDING) {
            client.startPicking(orderId);
            status = orderService.getOrder(orderId).getStatus();
        }

        if (status == OrderStatus.PICKING_IN_PROGRESS) {
            int itemCount = order.getLines().stream().mapToInt(OrderLine::getQuantity).sum();
            client.pick(orderId, Math.max(itemCount, 1));
            status = orderService.getOrder(orderId).getStatus();
        }

        JsonNode shipment = null;
        if (status == OrderStatus.PICKED || status == OrderStatus.SHIPPING_PENDING) {
            shipment = client.ship(orderId, DEFAULT_CARRIER);
            order = orderService.getOrder(orderId);
            status = order.getStatus();
            notifyWithRetry(order, shipment.path("trackingNumber").asText(""), shipment.path("carrier").asText(DEFAULT_CARRIER));
        }

        if (status == OrderStatus.SHIPPED || status == OrderStatus.IN_DELIVERY || status == OrderStatus.OUT_FOR_DELIVERY) {
            if (shipment == null) {
                shipment = client.findShipment(orderId);
            }
            client.deliver(orderId, shipment.path("id").asLong(), "Customer #" + order.getCustomerId());
            status = orderService.getOrder(orderId).getStatus();
        }

        if (status == OrderStatus.DELIVERED) {
            log.info("Order {} is DELIVERED - fulfilment finished", orderId);
        } else {
            log.info("Order {} ended fulfilment at status {}", orderId, status);
        }
    }

    private void notifyWithRetry(Order order, String trackingNumber, String carrier) {
        for (int attempt = 1; attempt <= NOTIFY_ATTEMPTS; attempt++) {
            try {
                client.notifyShipmentCompleted(order.getId(), order.getCustomerId(), trackingNumber, carrier);
                return;
            } catch (RuntimeException ex) {
                log.warn("Shipment notification for order {} failed (attempt {}/{}): {}", order.getId(), attempt, NOTIFY_ATTEMPTS, ex.getMessage());
                if (attempt == NOTIFY_ATTEMPTS) {
                    log.error("Order {} shipped but the ShipmentCompleted event could not be published", order.getId());
                    return;
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
