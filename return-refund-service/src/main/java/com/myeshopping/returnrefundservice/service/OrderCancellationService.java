package com.myeshopping.returnrefundservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.myeshopping.returnrefundservice.client.OrderClient;
import com.myeshopping.returnrefundservice.dto.ReturnRequest;
import com.myeshopping.returnrefundservice.entity.ReturnRequestEntity;
import com.myeshopping.returnrefundservice.entity.ReturnStatus;
import com.myeshopping.returnrefundservice.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cancels an order (before delivery) or returns it (after delivery) and refunds it.
 * Not delivered: order CANCELLED then REFUNDED.  Delivered (within 2 days): order RETURNED then REFUNDED.
 * Safe to call again: a half-finished cancellation resumes where it stopped.
 */
@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private static final Logger log = LoggerFactory.getLogger(OrderCancellationService.class);
    private static final Duration RETURN_WINDOW = Duration.ofDays(2);

    private final ReturnRefundService returnRefundService;
    private final ReturnRequestRepository repository;
    private final OrderClient orderClient;

    public Map<String, Object> cancelOrReturn(Long orderId, String reason) {
        JsonNode order = orderClient.getOrder(orderId);
        String status = order.path("status").asText();
        Long customerId = order.path("customerId").asLong();
        double amount = order.path("totalAmount").asDouble();

        if ("REFUNDED".equals(status)) {
            throw new IllegalArgumentException("This order has already been refunded");
        }
        boolean delivered = "DELIVERED".equals(status) || "RETURNED".equals(status);
        if ("DELIVERED".equals(status)) {
            LocalDateTime deliveredAt = LocalDateTime.parse(order.path("updatedAt").asText());
            if (Duration.between(deliveredAt, LocalDateTime.now()).compareTo(RETURN_WINDOW) > 0) {
                throw new IllegalArgumentException("The 2-day return window after delivery has closed for this order");
            }
        }

        ReturnRequestEntity request = repository.findByOrderId(orderId).orElseGet(() -> {
            ReturnRequest created = new ReturnRequest();
            created.setOrderId(orderId);
            created.setCustomerId(customerId);
            created.setReason(reason == null || reason.isBlank() ? "Cancelled by customer" : reason.trim());
            created.setAmount(amount);
            return returnRefundService.create(created);
        });

        // 1) the order itself: CANCELLED (not delivered) or RETURNED (delivered); order-service also releases the stock
        if (!"CANCELLED".equals(status) && !"RETURNED".equals(status)) {
            orderClient.transition(orderId, delivered ? "RETURNED" : "CANCELLED");
        }

        // 2) the return / refund record: walk it through to REFUNDED
        while (request.getStatus() != ReturnStatus.REFUNDED) {
            request = switch (request.getStatus()) {
                case RETURN_REQUESTED -> returnRefundService.transition(request.getId(), (delivered ? ReturnStatus.RETURN_APPROVED : ReturnStatus.CANCELLED).name());
                case RETURN_APPROVED -> returnRefundService.transition(request.getId(), ReturnStatus.RETURN_PICKUP_PENDING.name());
                case RETURN_PICKUP_PENDING -> returnRefundService.transition(request.getId(), ReturnStatus.RETURNED.name());
                case RETURNED, CANCELLED -> returnRefundService.refund(request.getId(), ReturnStatus.REFUND_REQUESTED.name());
                case REFUND_REQUESTED -> returnRefundService.refund(request.getId(), ReturnStatus.REFUND_PROCESSING.name());
                case REFUND_PROCESSING -> returnRefundService.refund(request.getId(), ReturnStatus.REFUNDED.name());
                default -> throw new IllegalArgumentException("This order's return cannot be processed (" + request.getStatus() + ")");
            };
        }

        // 3) the order is refunded
        orderClient.transition(orderId, "REFUNDED");
        log.info("Order {} {} and refunded ({})", orderId, delivered ? "returned" : "cancelled", amount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("outcome", delivered ? "RETURNED_AND_REFUNDED" : "CANCELLED_AND_REFUNDED");
        result.put("orderStatus", "REFUNDED");
        result.put("refundAmount", amount);
        result.put("returnRequestId", request.getId());
        return result;
    }
}