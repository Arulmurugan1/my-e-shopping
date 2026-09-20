package com.myeshopping.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.notification.config.KafkaTopicsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /** Publishes a ShipmentCompleted event to the shipment-events Kafka topic. */
    @PostMapping("/shipment-completed")
    public ResponseEntity<Map<String, Object>> shipmentCompleted(@RequestBody ShipmentCompletedRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (request.orderId() == null) {
            result.put("success", false);
            result.put("message", "orderId is required");
            return ResponseEntity.badRequest().body(result);
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("orderId", request.orderId());
            payload.put("customerId", request.customerId());
            payload.put("trackingNumber", request.trackingNumber());
            payload.put("carrier", request.carrier());

            String eventId = UUID.randomUUID().toString();
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventId", eventId);
            event.put("eventType", "ShipmentCompleted");
            event.put("timestamp", Instant.now().toString());
            event.put("correlationId", MDC.get("correlationId"));
            event.put("aggregateId", "order-" + request.orderId());
            event.put("version", 1);
            event.put("payload", payload);

            kafkaTemplate.send(KafkaTopicsConfig.SHIPMENT_EVENTS, String.valueOf(request.orderId()),
                    objectMapper.writeValueAsString(event)).get(10, TimeUnit.SECONDS);

            log.info("ShipmentCompleted event published for order {} (tracking {})", request.orderId(), request.trackingNumber());
            result.put("success", true);
            result.put("message", "ShipmentCompleted event published");
            result.put("eventId", eventId);
            result.put("topic", KafkaTopicsConfig.SHIPMENT_EVENTS);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Could not publish ShipmentCompleted for order {}: {}", request.orderId(), ex.getMessage());
            result.put("success", false);
            result.put("message", "Could not publish the shipment event: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }
    }
}