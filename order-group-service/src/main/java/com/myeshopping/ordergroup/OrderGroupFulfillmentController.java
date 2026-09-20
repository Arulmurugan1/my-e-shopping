package com.myeshopping.ordergroup;

import com.myeshopping.ordergroup.client.OrderStatusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Takes a newly created order into picking. */
@RestController
@RequestMapping("/api/v1/order-groups/orders")
public class OrderGroupFulfillmentController {

    private static final Logger log = LoggerFactory.getLogger(OrderGroupFulfillmentController.class);
    private static final List<String> CHAIN = List.of("ORDERED", "PICKING_PENDING", "PICKING_IN_PROGRESS");

    private final OrderStatusClient orderStatusClient;

    public OrderGroupFulfillmentController(OrderStatusClient orderStatusClient) {
        this.orderStatusClient = orderStatusClient;
    }

    @PostMapping("/{orderId}/start-picking")
    public Map<String, Object> startPicking(@PathVariable Long orderId) {
        log.info("Grouping order {} and sending it to picking", orderId);
        orderStatusClient.advance(orderId, CHAIN);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("status", "PICKING_IN_PROGRESS");
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        log.warn("start-picking rejected: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}