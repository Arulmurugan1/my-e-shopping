# Local Platform Services

## Start PostgreSQL, Kafka, and Redis

From the repository root:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

The platform exposes:

- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Redis: `localhost:6379`
- Prometheus: `localhost:9090`
- Grafana: `localhost:3000`

PostgreSQL creates one database per JPA service on first initialization. The credentials are `myeshopping` / `myeshopping`.

## Run a service against PostgreSQL

The existing H2 configuration remains the default for tests. Activate the PostgreSQL profile when running a service:

```powershell
mvn.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```

For example, Order Service uses `orderdb` and publishes `OrderCreated` events to the `order-events` Kafka topic. Payment Service uses `paymentdb` and Redis keys with the prefix `payment:idempotency:` and a 24-hour TTL.

## Reset PostgreSQL initialization

The database creation script runs only when the PostgreSQL data volume is empty. To recreate all databases from scratch:

```powershell
docker compose -f infrastructure/docker/docker-compose.yml down -v
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Do not use `down -v` if you need to preserve local data.
