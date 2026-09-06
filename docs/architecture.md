# High-Level Architecture

## 1. Overview

My E-Shopping is a distributed, event-driven e-supermarket platform designed to handle customer shopping, cart management, order orchestration, payment, inventory control, warehouse operations, shipment, delivery, invoice generation, returns, notifications, and operational observability.

The platform follows a microservice architecture with one business capability per service and shared infrastructure for messaging, persistence, caching, monitoring, and coordination.

## 2. Architectural goals

- Scale individual service workloads independently
- Isolate business logic and data ownership
- Support asynchronous workflows using Kafka
- Leverage resilience patterns for dependency failures
- Keep transaction boundaries local to each service
- Provide strong observability and auditing through correlation IDs and structured logs
- Keep customer and admin experiences simple via API Gateway and Angular frontend

## 3. Core patterns

- API Gateway for external traffic
- REST for synchronous client and service communication
- OpenFeign for service-to-service calls when clear contracts exist
- Kafka for async event-driven orchestration
- Service-owned PostgreSQL databases
- Redis for temporary cache, idempotency, and fast access data
- Saga pattern for long-running business flows
- JWT-based authentication and role-based access control
- Docker Compose for local infrastructure
- Kubernetes/EKS for production deployment

## 4. Service responsibilities

### API Gateway
- Routes external requests
- Validates JWT tokens
- Applies rate limits and correlation IDs
- Centralizes logging and error handling

### Auth / User Service
- User registration and login
- Password hashing and JWT issuance
- Role management and user activation status
- Customer/admin identity flows

### Customer Service
- Customer profile, address management, dashboard data, status aggregation
- Customer-specific business queries

### Inventory Service
- Product catalog and inventory management
- Stock, reserved, available quantities
- Soft-deletes and reservation handling

### Cart Service
- Cart creation, add/edit/remove item
- Cart aggregation and pricing calculations
- Checkout validation

### Order Service
- Order lifecycle and domain orchestration
- Business rule validation and status transitions

### Payment Service
- Payment initiation, status tracking, refunds
- Mock payment provider integration
- Idempotency enforcement

### Order Group Service
- Groups orders by customer and delivery date
- Handles grouping idempotency

### Cartonization Service
- Determines carton count, sizing, and package planning

### Picking Service
- Warehouse picking lifecycle and status transitions

### Shipment Service
- Shipment creation, tracking, and manual completion

### Delivery Service
- Delivery progression and final confirmation

### Invoice Service
- Invoice generation and retrieval

### Return / Refund Service
- Return request management and refund lifecycle

### Notification Service
- Sends email/mock notifications based on Kafka events

### Logging Service
- Structured logging ingestion and centralized log processing

### Audit Service
- Stores audit records for operational actions and business events

### Scheduler Service
- Evaluates job configuration and executes periodic tasks
- Records execution details and skips disabled jobs

## 5. Communication model

### Synchronous communication
- Frontend -> API Gateway -> Service APIs
- Service -> Service via OpenFeign for direct queries and validation
- Mostly used for request/response flows like order creation, payment inquiry, and user data lookup

### Asynchronous communication
- Kafka topics publish domain events
- Consumer services react idempotently and compensate where required

## 6. Domain event flow

The application uses events such as:

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

Every event includes:
- eventId
- eventType
- timestamp
- correlationId
- aggregateId
- version
- payload

## 7. State machines

### Order state machine
ORDERED -> PICKING_PENDING -> PICKING_IN_PROGRESS -> PICKED -> SHIPPING_PENDING -> SHIPPED -> IN_DELIVERY -> OUT_FOR_DELIVERY -> DELIVERED

### Payment state machine
INITIATED -> SUCCESS | FAILED -> REFUND_PENDING -> REFUNDED | REFUND_CANCELLED | REFUND_FAILED

### Return / refund state machine
RETURN_REQUESTED -> RETURN_APPROVED | RETURN_REJECTED -> RETURN_PICKUP_PENDING -> RETURNED -> RETURN_CANCELLED

### Picking, shipment, delivery flows
Each workflow is explicitly transition-validated and must reject invalid transitions.

## 8. Database strategy

- Each microservice owns its own PostgreSQL schema/database, without cross-service database access
- Use Flyway or Liquibase for schema evolution
- Use UUIDs and timestamps where appropriate
- Enforce foreign keys and indexes inside the service-owned boundary
- Soft delete for products and other entities where pending relationships must be preserved

## 9. SAGA / transaction strategy

The system avoids distributed database transactions. Instead, it uses a saga style with:

1. Local transaction in a service
2. Event emitted to Kafka
3. Downstream services perform their own processing and compensation when needed
4. Idempotency keys to prevent duplicate effects

## 10. Security model

- Spring Security + JWT
- BCrypt password hashing
- Role-based access: ADMIN, CUSTOMER
- Expiration + inactivity checks
- HTTPS-ready configuration and CORS tuning
- No secrets or credentials are logged

## 11. Observability plan

- Spring Boot Actuator endpoints
- Micrometer metrics
- Prometheus scraping
- Grafana dashboards
- Structured JSON logging
- Correlation IDs across requests and Kafka events
- Alerts for error rate, latency, Kafka lag, circuit breaker state, and scheduler failures

## 12. Deployment model

### Local
- Docker Compose with PostgreSQL, Kafka, Redis, Prometheus, Grafana

### Production-ready
- Kubernetes deployments and services
- EKS + RDS PostgreSQL + ElastiCache Redis + MSK Kafka + S3 for files + CloudWatch + IAM

## 13. CI/CD

GitHub Actions pipeline stages:
1. Checkout
2. Build
3. Unit tests
4. Integration tests
5. Static analysis
6. Package
7. Docker build
8. Image scan
9. Push image
10. Kubernetes deployment

## 14. Development phases

1. Architecture and repo structure
2. Auth/User Service
3. Customer Service
4. Inventory Service
5. Cart Service
6. Order Service
7. Payment Service
8. Kafka event architecture
9. Order Grouping and Cartonization
10. Picking
11. Shipment
12. Delivery
13. Invoice
14. Return/Refund
15. Notification
16. Logging and Audit
17. Scheduler
18. Angular frontend
19. Docker Compose
20. Kubernetes
21. Prometheus/Grafana
22. CI/CD
23. Testing and integration

## 15. Risks and recommendations

- Do not implement all services in one pass; phase the build
- Keep shared contracts versioned and documented
- Start with core order lifecycle before expanding warehouse flows
- Validate idempotency before scaling Kafka consumers
- Design strict state transitions early to prevent invalid business operations
- Add centralized audit and correlation logging from the beginning

## 16. Approval gate

This architecture should be reviewed before any implementation phase begins. Once approved, the work can proceed one phase/service at a time, following the project specification and quality gates.
