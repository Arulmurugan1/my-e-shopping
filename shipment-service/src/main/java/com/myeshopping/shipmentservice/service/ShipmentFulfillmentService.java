package com.myeshopping.shipmentservice.service;

import com.myeshopping.shipmentservice.client.OrderStatusClient;
import com.myeshopping.shipmentservice.dto.ShipmentRequest;
import com.myeshopping.shipmentservice.entity.Shipment;
import com.myeshopping.shipmentservice.entity.ShipmentStatus;
import com.myeshopping.shipmentservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Ships an order end to end: create the shipment, mark the order SHIPPED, then complete the shipment. */
@Service
@RequiredArgsConstructor
public class ShipmentFulfillmentService {

    private static final List<String> ORDER_CHAIN = List.of("PICKED", "SHIPPING_PENDING", "SHIPPED");

    private final ShipmentService shipmentService;
    private final ShipmentRepository repository;
    private final OrderStatusClient orderStatusClient;

    public Shipment fulfill(Long orderId, String carrier) {
        Shipment shipment = repository.findByOrderId(orderId).orElseGet(() -> {
            ShipmentRequest request = new ShipmentRequest();
            request.setOrderId(orderId);
            request.setCarrier(carrier);
            return shipmentService.create(request);
        });
        orderStatusClient.advance(orderId, ORDER_CHAIN);
        if (shipment.getStatus() != ShipmentStatus.COMPLETED) {
            shipment = shipmentService.complete(shipment.getId());
        }
        return shipment;
    }
}