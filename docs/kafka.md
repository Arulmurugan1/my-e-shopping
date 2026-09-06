# Kafka Topic and Event Design

## Topics

- order-events
- payment-events
- inventory-events
- picking-events
- shipment-events
- delivery-events
- return-events
- refund-events
- notification-events
- audit-events
- scheduler-events
- logging-events

## Event envelope

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "timestamp": "2026-09-06T00:00:00Z",
  "correlationId": "uuid",
  "aggregateId": "order-123",
  "version": 1,
  "payload": {}
}
```

## Example events

- OrderCreated
- OrderCancelled
- PaymentCompleted
- PaymentFailed
- InventoryReserved
- InventoryReleased
- PickingStarted
- PickingCompleted
- ShipmentCreated
- ShipmentCompleted
- DeliveryStarted
- DeliveryCompleted
- ReturnRequested
- RefundRequested
- NotificationRequested

## Consumer rules

- Consumers must be idempotent
- Retry transient failures only
- Publish compensation events for rollback scenarios
- Maintain correlation IDs across all tracing
