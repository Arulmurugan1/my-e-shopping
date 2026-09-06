# State Machines

## Order

```text
ORDERED
  -> PICKING_PENDING
  -> PICKING_IN_PROGRESS
  -> PICKED
  -> SHIPPING_PENDING
  -> SHIPPED
  -> IN_DELIVERY
  -> OUT_FOR_DELIVERY
  -> DELIVERED
```

Invalid transitions are rejected explicitly.

## Payment

```text
INITIATED
  -> SUCCESS
  -> REFUND_PENDING
  -> REFUNDED

INITIATED
  -> FAILED
  -> REFUND_CANCELLED

REFUND_PENDING
  -> REFUND_FAILED
```

## Return / Refund

```text
RETURN_REQUESTED
  -> RETURN_APPROVED
  -> RETURN_PICKUP_PENDING
  -> RETURNED

RETURN_REQUESTED
  -> RETURN_REJECTED
  -> RETURN_CANCELLED
```

```text
REFUND_REQUESTED
  -> REFUND_PROCESSING
  -> REFUNDED

REFUND_REQUESTED
  -> REFUND_CANCELLED
  -> REFUND_FAILED
```

## Business-rule summary

- Order cancellation only before shipment completion
- Inventory soft deletes when linked to pending orders
- Payment must complete before final order placement
- Inventory reserved before confirmation
- Duplicate events must not duplicate business effects
- Inactive users cannot participate in auth flows
