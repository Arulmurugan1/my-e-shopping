package com.myeshopping.notification;

public record ShipmentCompletedRequest(Long orderId, Long customerId, String trackingNumber, String carrier) {
}