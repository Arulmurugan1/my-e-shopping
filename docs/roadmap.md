# Development Roadmap

## Phase 1 — Architecture and repository structure
- Complete architecture document
- Define service responsibilities and communication model
- Create repository skeleton
- Validate scope and sequencing

## Phase 2 — Auth and customer foundation
- User registration, login, JWT
- Customer profile model
- Admin and customer role handling

## Phase 3 — Inventory and cart
- Product catalogs and inventory
- Cart lifecycle and checkout validation

## Phase 4 — Order lifecycle and payment
- Order orchestration
- Payment flow and idempotency
- Order confirmation and cancellation

## Phase 5 — Warehouse and delivery operations
- Grouping, cartonization, picking, shipment, delivery

## Phase 6 — Returns, invoices, and notifications
- Invoice generation
- Return/refund processing
- Kafka-driven notifications

## Phase 7 — Logging, audit, and scheduler
- Centralized logging
- Audit record processing
- Scheduler jobs and logs

## Phase 8 — Frontend and platform ops
- Angular customer/admin application
- Docker compose
- Kubernetes manifests
- Prometheus/Grafana dashboards
- GitHub Actions pipeline

## Phase 9 — Integration and quality
- Unit tests and integration tests
- Security validation
- State-machine verification
- End-to-end smoke checks
