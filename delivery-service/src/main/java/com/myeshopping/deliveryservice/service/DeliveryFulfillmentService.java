package com.myeshopping.deliveryservice.service;

import com.myeshopping.deliveryservice.client.OrderStatusClient;
import com.myeshopping.deliveryservice.dto.DeliveryRequest;
import com.myeshopping.deliveryservice.entity.Delivery;
import com.myeshopping.deliveryservice.entity.DeliveryStatus;
import com.myeshopping.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Delivers a shipped order: delivery CREATED -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED, then the order becomes DELIVERED. */
@Service
@RequiredArgsConstructor
public class DeliveryFulfillmentService {

    private static final List<String> ORDER_CHAIN = List.of("SHIPPED", "IN_DELIVERY", "OUT_FOR_DELIVERY", "DELIVERED");

    private final DeliveryService deliveryService;
    private final DeliveryRepository repository;
    private final OrderStatusClient orderStatusClient;

    public Delivery fulfill(Long orderId, Long shipmentId, String recipientName) {
        Delivery delivery = repository.findByShipmentId(shipmentId).orElseGet(() -> {
            DeliveryRequest request = new DeliveryRequest();
            request.setShipmentId(shipmentId);
            request.setRecipientName(recipientName);
            return deliveryService.create(request);
        });
        if (delivery.getStatus() == DeliveryStatus.CREATED) {
            delivery = deliveryService.transition(delivery.getId(), DeliveryStatus.IN_TRANSIT.name());
        }
        if (delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
            delivery = deliveryService.transition(delivery.getId(), DeliveryStatus.OUT_FOR_DELIVERY.name());
        }
        if (delivery.getStatus() == DeliveryStatus.OUT_FOR_DELIVERY) {
            delivery = deliveryService.transition(delivery.getId(), DeliveryStatus.DELIVERED.name());
        }
        orderStatusClient.advance(orderId, ORDER_CHAIN);
        return delivery;
    }
}