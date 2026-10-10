package com.myeshopping.invoiceservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(@Value("${customer-service.base-url:http://localhost:8091}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Returns the auth userId linked to a customer profile id. */
    public String getUserId(Long customerId) {
        try {
            JsonNode response = restClient.get().uri("/api/v1/customers/profiles/{id}", customerId)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) { headers.set("X-Correlation-ID", correlationId); }
                    })
                    .retrieve().body(JsonNode.class);
            String userId = response == null ? "" : response.path("data").path("userId").asText("");
            if (userId.isBlank()) {
                throw new IllegalArgumentException("Customer not found: " + customerId);
            }
            return userId;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Customer service is unavailable. Please try again shortly.");
        }
    }
}
