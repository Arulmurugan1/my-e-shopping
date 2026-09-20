package com.myeshopping.invoiceservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${order-service.base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Returns the order's data node: id, customerId, shippingAddress, status, totalAmount, createdAt, lines[]. */
    public JsonNode getOrder(Long orderId) {
        try {
            JsonNode response = restClient.get().uri("/api/v1/orders/{id}", orderId)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) { headers.set("X-Correlation-ID", correlationId); }
                    })
                    .retrieve().body(JsonNode.class);
            JsonNode data = response == null ? null : response.path("data");
            if (data == null || data.isMissingNode() || data.isNull()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            return data;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Order service is unavailable. Please try again shortly.");
        }
    }
}