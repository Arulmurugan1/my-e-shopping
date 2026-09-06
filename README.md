# My E-Shopping

This repository contains the high-level architecture and implementation blueprint for the My E-Shopping e-supermarket platform described in the master project specification.

## Phase status

- Phase 1: Architecture and repository structure — complete
- Phases 2-7: Core services and operational APIs — implemented locally
- Phase 8: API Gateway, Angular frontend, Docker dependencies, Prometheus, and CI — implemented
- Phase 9: Focused service smoke tests — passing for implemented tested services

Local service persistence currently uses H2. Kafka event consumers/producers, PostgreSQL migrations, Kubernetes manifests, AWS resources, and production security hardening remain platform follow-up work.

## Repository layout

```text
my-e-shopping/
├── api-gateway/
├── auth-service/
├── customer-service/
├── order-service/
├── inventory-service/
├── cart-service/
├── cartonization-service/
├── order-group-service/
├── picking-service/
├── shipment-service/
├── delivery-service/
├── payment-service/
├── notification-service/
├── invoice-service/
├── return-refund-service/
├── logging-service/
├── audit-service/
├── scheduler-service/
├── frontend/
│   └── angular-app/
├── infrastructure/
│   ├── docker/
│   ├── kubernetes/
│   ├── kafka/
│   ├── postgres/
│   ├── redis/
│   ├── prometheus/
│   └── grafana/
├── .github/
│   └── workflows/
├── README.md
└── docs/
    ├── architecture.md
    ├── microservices.md
    ├── kafka.md
    ├── state-machines.md
    ├── security.md
    ├── deployment.md
    └── roadmap.md
```

## Architecture note

The project follows the requirement set defined in the master project specification: Java 17 + Spring Boot 3.5, Spring Cloud, PostgreSQL, Kafka, Redis, Docker, Kubernetes, AWS deployment model, Angular frontend, and a microservice-oriented event-driven architecture.

The local entry point is the API Gateway on port 8080. Start the required backend services first, then run the Angular app from `frontend/angular-app` with `npm.cmd start`.
