package com.myeshopping.orderservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Calls the services that fulfil an order, in order: order-group, picking, shipment, notification. */
@Component
public class FulfillmentClient {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentClient.class);

    private final RestClient orderGroup;
    private final RestClient picking;
    private final RestClient shipment;
    private final RestClient notification;
    private final RestClient delivery;

    public FulfillmentClient(@Value("${order-group-service.base-url:http://localhost:8093}") String orderGroupUrl,
                             @Value("${picking-service.base-url:http://localhost:8086}") String pickingUrl,
                             @Value("${shipment-service.base-url:http://localhost:8087}") String shipmentUrl,
                             @Value("${notification-service.base-url:http://localhost:8094}") String notificationUrl,
                             @Value("${delivery-service.base-url:http://localhost:8088}") String deliveryUrl) {
        this.orderGroup = RestClient.builder().baseUrl(orderGroupUrl).build();
        this.picking = RestClient.builder().baseUrl(pickingUrl).build();
        this.shipment = RestClient.builder().baseUrl(shipmentUrl).build();
        this.notification = RestClient.builder().baseUrl(notificationUrl).build();
        this.delivery = RestClient.builder().baseUrl(deliveryUrl).build();
    }

    /** order-group-service moves the order to PICKING_IN_PROGRESS. */
    public void startPicking(Long orderId) {
        post(orderGroup, "order-group-service", "/api/v1/order-groups/orders/{id}/start-picking", orderId, null);
    }

    /** picking-service picks the order and moves it to PICKED. */
    public void pick(Long orderId, int itemCount) {
        post(picking, "picking-service", "/api/v1/picking/orders/{id}/fulfill?itemCount=" + itemCount, orderId, null);
    }

    /** shipment-service ships the order and moves it to SHIPPED. Returns the shipment (with tracking number). */
    public JsonNode ship(Long orderId, String carrier) {
        return post(shipment, "shipment-service", "/api/v1/shipments/orders/{id}/fulfill?carrier=" + carrier, orderId, null).path("data");
    }

    /** notification-service publishes the ShipmentCompleted event to Kafka. */
    public void notifyShipmentCompleted(Long orderId, Long customerId, String trackingNumber, String carrier) {
        post(notification,
            "notification-service", 
            "/api/v1/notifications/shipment-completed", 
            orderId,
            Map.of("orderId", orderId, 
                    "customerId", customerId, 
                    "trackingNumber", trackingNumber, 
                    "carrier", carrier)
            );
    }

    /** delivery-service delivers the order and moves it to DELIVERED. */
    public void deliver(Long orderId, Long shipmentId, String recipientName) {
        post(delivery, "delivery-service",
                "/api/v1/deliveries/orders/{id}/fulfill?shipmentId=" + shipmentId + "&recipientName={name}",
                orderId, null, recipientName);
    }

    /** Looks up the shipment already created for an order (used when a run resumes after shipping). */
    public JsonNode findShipment(Long orderId) {
        try {
            JsonNode response = shipment.get().uri("/api/v1/shipments/order/{id}", orderId).retrieve().body(JsonNode.class);
            return response == null ? com.fasterxml.jackson.databind.node.MissingNode.getInstance() : response.path("data");
        } catch (RestClientException ex) {
            throw new IllegalStateException("shipment-service could not find the shipment for order " + orderId + ": " + ex.getMessage(), ex);
        }
    }

    private static Object[] uriVariables(Long orderId, Object[] extra) {
        Object[] all = new Object[1 + extra.length];
        all[0] = orderId;
        System.arraycopy(extra, 0, all, 1, extra.length);
        return all;
    }

    private JsonNode post(RestClient client, String service, String uri, Long orderId, Object body, Object... extraUriVariables) {
        try {
            RestClient.RequestBodySpec request = client.post()
                    .uri(uri, uriVariables(orderId, extraUriVariables))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        String correlationId = MDC.get("correlationId");
                        if (correlationId != null) { headers.set("X-Correlation-ID", correlationId); }
                    });
            if (body != null) { request.body(body); }
            JsonNode response = request.retrieve().body(JsonNode.class);
            log.info("{} completed its step for order {}", service, orderId);
            return response == null ? com.fasterxml.jackson.databind.node.MissingNode.getInstance() : response;
        } catch (RestClientException ex) {
            throw new IllegalStateException(service + " failed for order " + orderId + ": " + ex.getMessage(), ex);
        }
    }
}
