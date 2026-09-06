# Microservice Responsibility Matrix

| Service | Primary responsibility | Key data | Main integrations |
|---|---|---|---|
| api-gateway | Request routing and auth validation | Routing rules, security policies | All services |
| auth-service | Authentication, JWT, roles, user access | Users, credentials, auth metadata | customer-service, gateway |
| customer-service | Customer profiles, addresses, dashboard data | Customer records, addresses | auth-service, order-service |
| inventory-service | Product catalog and stock management | Products, inventory, reservations | order-service, cart-service |
| cart-service | Cart state and price summary | Cart items, pricing context | inventory-service, order-service |
| order-service | Order workflow orchestration | Orders, statuses, transitions | inventory-service, payment-service, kafka |
| payment-service | Payment and refund processing | Payment records, refunds | order-service, notification-service |
| order-group-service | grouping by customer/delivery date | Order groups, group membership | order-service |
| cartonization-service | Packaging optimization metadata | Cartons, package rules | order-service |
| picking-service | Warehouse picking workflows | Pick tasks, item allocation | order-service, shipment-service |
| shipment-service | Shipment creation and tracking | Shipments, tracking number | picking-service, delivery-service |
| delivery-service | Delivery lifecycle | Delivery records, timestamps | shipment-service |
| invoice-service | Invoice generation and retrieval | Invoices | order-service, payment-service |
| return-refund-service | Returns and refunds | Return requests, refund records | order-service, payment-service |
| notification-service | Email and notification processing | Notification events | kafka, customer-service |
| logging-service | Centralized logs | Structured logs | all services |
| audit-service | Audit records | Audit entries | all services |
| scheduler-service | Periodic business job execution | Scheduler config, job logs | all services |

## Communication strategy

- REST for client-facing and request/response operations
- OpenFeign for synchronous service calls
- Kafka for asynchronous event-driven communication
- GraphQL for dashboard aggregation read paths only
