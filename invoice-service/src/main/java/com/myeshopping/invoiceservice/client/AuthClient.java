package com.myeshopping.invoiceservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(@Value("${auth-service.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Returns the login email for an auth userId. */
    public String getEmail(String userId) {
        try {
            JsonNode response = restClient.get().uri("/api/v1/auth/internal/users/{id}/email", userId)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) { headers.set("X-Correlation-ID", correlationId); }
                    })
                    .retrieve().body(JsonNode.class);
            String email = response == null ? "" : response.path("data").asText("");
            if (email.isBlank()) {
                throw new IllegalArgumentException("User not found: " + userId);
            }
            return email;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalArgumentException("User not found: " + userId);
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Auth service is unavailable. Please try again shortly.");
        }
    }
}
