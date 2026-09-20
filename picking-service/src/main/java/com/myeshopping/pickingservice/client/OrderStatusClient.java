package com.myeshopping.pickingservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/** Moves an order along its status chain in order-service, resuming from wherever the order currently is. */
@Component
public class OrderStatusClient {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusClient.class);

    private final RestClient restClient;

    public OrderStatusClient(@Value("${order-service.base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * @param chain the statuses this step walks through, e.g. [ORDERED, PICKING_PENDING, PICKING_IN_PROGRESS].
     *              The order must currently be at one of them; every later status in the chain is applied in turn.
     */
    public void advance(Long orderId, List<String> chain) {
        try {
            JsonNode order = restClient.get()
                    .uri("/api/v1/orders/{id}", orderId)
                    .headers(this::addCorrelationId)
                    .retrieve()
                    .body(JsonNode.class);
            String current = order == null ? "" : order.path("data").path("status").asText("");
            int index = chain.indexOf(current);
            if (index < 0) {
                throw new IllegalArgumentException("Order " + orderId + " is " + current + ", expected one of " + chain);
            }
            for (String next : chain.subList(index + 1, chain.size())) {
                restClient.post()
                        .uri("/api/v1/orders/{id}/transition", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(this::addCorrelationId)
                        .body(Map.of("status", next))
                        .retrieve()
                        .toBodilessEntity();
                log.info("Order {} status changed to {}", orderId, next);
            }
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Could not update the status of order " + orderId + ": " + ex.getMessage());
        }
    }

    private void addCorrelationId(org.springframework.http.HttpHeaders headers) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            headers.set("X-Correlation-ID", correlationId);
        }
    }
}