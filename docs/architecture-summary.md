# Architecture Summary

## Scope

This project is designed as a scalable Java 17 microservice e-commerce platform with Angular frontend and cloud-native infrastructure.

## Architectural decisions

- service-oriented decomposition by business capability
- Kafka-based event-driven integration for asynchronous domains
- PostgreSQL as service-local transactional source of truth
- Redis for temporary high-speed data and idempotency
- JWT-based authentication and role-based integration
- Saga-style orchestration for sales and delivery workflows
- Docker and Kubernetes support for local and production deployment
- Prometheus/Grafana-driven observability
- GitHub Actions-based CI/CD pipeline

## Approval status

Pending review before implementation begins.

## Next step

Review the architecture and docs in this folder, then confirm approval to start Phase 2 implementation.
