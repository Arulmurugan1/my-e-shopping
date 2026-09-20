package com.myeshopping.pickingservice.service;

import com.myeshopping.pickingservice.client.OrderStatusClient;
import com.myeshopping.pickingservice.dto.PickingRequest;
import com.myeshopping.pickingservice.entity.PickingStatus;
import com.myeshopping.pickingservice.entity.PickingTask;
import com.myeshopping.pickingservice.repository.PickingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Picks an order end to end: PENDING -> IN_PROGRESS -> COMPLETED, then marks the order PICKED. */
@Service
@RequiredArgsConstructor
public class PickingFulfillmentService {

    private static final List<String> ORDER_CHAIN = List.of("PICKING_IN_PROGRESS", "PICKED");

    private final PickingService pickingService;
    private final PickingTaskRepository repository;
    private final OrderStatusClient orderStatusClient;

    public PickingTask fulfill(Long orderId, int itemCount) {
        PickingTask task = repository.findByOrderId(orderId).orElseGet(() -> {
            PickingRequest request = new PickingRequest();
            request.setOrderId(orderId);
            request.setItemCount(Math.max(itemCount, 1));
            return pickingService.create(request);
        });
        if (task.getStatus() == PickingStatus.PENDING) {
            task = pickingService.transition(task.getId(), PickingStatus.IN_PROGRESS.name());
        }
        if (task.getStatus() == PickingStatus.IN_PROGRESS) {
            task = pickingService.transition(task.getId(), PickingStatus.COMPLETED.name());
        }
        orderStatusClient.advance(orderId, ORDER_CHAIN);
        return task;
    }
}