package com.myeshopping.returnrefundservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class OrderClient {

    private static final Logger log = LoggerFactory.getLogger(OrderClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderClient(@Value("${order-service.base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Returns the order's data node (id, customerId, status, totalAmount, updatedAt, lines...). */
    public JsonNode getOrder(Long orderId) {
        try {
            JsonNode response = restClient.get().uri("/api/v1/orders/{id}", orderId)
                    .headers(this::addCorrelationId).retrieve().body(JsonNode.class);
            JsonNode data = response == null ? null : response.path("data");
            if (data == null || data.isMissingNode() || data.isNull()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            return data;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalArgumentException(messageOf(ex, "Order not found: " + orderId));
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Order service is unavailable. Please try again shortly.");
        }
    }

    public void transition(Long orderId, String status) {
        try {
            restClient.post().uri("/api/v1/orders/{id}/transition", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::addCorrelationId)
                    .body(Map.of("status", status))
                    .retrieve().toBodilessEntity();
            log.info("Order {} status changed to {}", orderId, status);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalArgumentException(messageOf(ex, "Could not change order " + orderId + " to " + status));
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Order service is unavailable. Please try again shortly.");
        }
    }

    private String messageOf(HttpStatusCodeException ex, String fallback) {
        try {
            String message = objectMapper.readTree(ex.getResponseBodyAsString()).path("message").asText("");
            return message.isBlank() ? fallback : message;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void addCorrelationId(HttpHeaders headers) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            headers.set("X-Correlation-ID", correlationId);
        }
    }
}