package com.myeshopping.orderservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void reserve(Long productId, String productName, int quantity) {
        send("reserve", productId, quantity, "Not enough stock for " + productName);
        log.info("Reserved {} unit(s) of product {} ({})", quantity, productId, productName);
    }

    public void release(Long productId, int quantity) {
        send("release", productId, quantity, "Could not release stock for product " + productId);
        log.info("Released {} unit(s) of product {}", quantity, productId);
    }

    private void send(String action, Long productId, int quantity, String failureMessage) {
        try {
            restClient.post()
                    .uri("/api/v1/inventory/{id}/{action}", productId, action)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) { headers.set("X-Correlation-ID", correlationId); }
                    })
                    .body(Map.of("quantity", quantity))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            log.warn("Inventory {} rejected for product {}: {}", action, productId, ex.getStatusCode());
            throw new IllegalArgumentException(failureMessage);
        } catch (ResourceAccessException ex) {
            log.error("Inventory service unreachable during {} for product {}", action, productId);
            throw new IllegalStateException("Inventory service is unavailable. Please try again shortly.");
        }
    }
}
