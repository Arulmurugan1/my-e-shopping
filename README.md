# My E-Shopping

An e-supermarket platform built as Spring Boot microservices behind an API gateway, with an Angular storefront, an admin area, and a full logging and metrics stack.

## What you can do

### Customers

- Sign in with a username, email or mobile number; register with email, username, mobile number, date of birth and gender
- Browse items (with pictures, live available stock and a filter box), add them to a cart with a quantity stepper, and place an order
- Success or failure popup on checkout; the cart clears after a successful order
- Home page with account details and order history; statuses update on their own as an order is picked and shipped
- Download a PDF invoice for any order
- Cancel an order at any point before delivery, or return it within 2 days after delivery; either way it is refunded

### Admins

- Users: active / inactive lists, activate or deactivate accounts, make or remove admins
- SKUs: add SKUs (with an optional image URL), set images, raise stock, deactivate
- Customers: everyone who has ordered, with order count and total spent, biggest first (cancelled orders excluded)

### Roles

| | Super admin | Admin | Customer |
|---|---|---|---|
| Activate or deactivate customers | yes | yes | no |
| Activate or deactivate admins | yes | no | no |
| Make or remove admins | yes | no | no |
| Change a super admin | never | never | never |

Nobody can change their own status or role. The super admin role cannot be assigned through the API; set it in the database (see below).

## Architecture

```text
Angular (4200) ──► API Gateway (8080) ──► auth, inventory, cart, order, customer, ... services
                                                │
                     order-service ──► inventory-service   (reserve stock on order, release on cancel)
```

| Port | Service | Database |
|---|---|---|
| 8080 | api-gateway | none |
| 8081 | auth-service | Oracle |
| 8082 | inventory-service | PostgreSQL |
| 8083 | cart-service | PostgreSQL |
| 8084 | order-service | PostgreSQL |
| 8085 | payment-service | PostgreSQL |
| 8086 | picking-service | PostgreSQL |
| 8087 | shipment-service | MySQL |
| 8088 | delivery-service | MySQL |
| 8089 | invoice-service | PostgreSQL |
| 8090 | return-refund-service | MySQL |
| 8091 | customer-service | PostgreSQL |
| 8092-8097 | cartonization, order-group, notification, logging, audit, scheduler | placeholders (no business code yet) |
| 4200 | Angular app | - |

Stock: placing an order reserves each item in inventory-service; if any item is short the whole order is rejected and earlier reservations are released. Cancelling an order releases the stock.

Fulfilment: placing an order publishes an `OrderCreated` event to the Kafka topic `order-events` (after the order is saved). A consumer in order-service then drives the order through, each service updating the order status itself:

1. order-group-service: `PICKING_IN_PROGRESS`
2. picking-service: `PICKED`
3. shipment-service: `SHIPPED`
4. notification-service: publishes `ShipmentCompleted` to the topic `shipment-events`
5. delivery-service: `IN_DELIVERY`, `OUT_FOR_DELIVERY`, then `DELIVERED`

The whole chain takes a few seconds, so in practice an order reaches `DELIVERED` almost immediately and can then only be returned (see below).

The consumer resumes from the order's current status if a step is retried. Kafka is only used when order-service runs with the `postgres` profile, which the ops dashboard sets for it automatically (start it by hand with `mvn spring-boot:run -Dspring-boot.run.profiles=postgres`).

Cancel and return: `return-refund-service` handles `POST /api/v1/returns/orders/{id}/cancel`. An order that has not been delivered goes `CANCELLED` then `REFUNDED`; a delivered order (within 2 days of delivery) goes `RETURNED` then `REFUNDED`. Stock is released either way. The refund is a record in the return-refund database; no payment gateway is involved yet.

Invoices: `invoice-service` builds the PDF on demand at `GET /api/v1/invoices/orders/{id}/pdf`.

## Running it locally

Prerequisites: Java 17+, Maven, Node.js, Docker Desktop.

1. **Start the infrastructure** (databases, Kafka, monitoring, logging):
   ```powershell
   docker compose -f infrastructure/docker/docker-compose.yml up -d
   ```
2. **Start the services.** Use the ops dashboard (below) and click *Start All Services*, or start one manually from its folder:
   ```powershell
   mvn spring-boot:run
   ```
   Start `api-gateway` last, or restart it if it started before the others.
3. **Start the frontend:**
   ```powershell
   cd frontend/angular-app
   npm.cmd install
   npm.cmd start
   ```
   Open http://localhost:4200.

### Ops dashboard

```powershell
dashboard\Live Server Status\start-live-dashboard.bat
```

Opens http://localhost:5055 with tabs for Instances (start / stop / restart with a progress bar, health, CPU and memory, plus URLs and login info for Grafana, Prometheus, OpenSearch, Kafka), Databases, Docker containers and Tables.

### Creating the first super admin

A super admin has to be set in the database, once. Register the account in the app first, then run this against the auth database (Oracle, service `XE`):

```sql
UPDATE users SET role = 'SUPER_ADMIN' WHERE email = 'you@example.com';
COMMIT;
```

Sign out and back in so the app picks up the new role. After that, admins are managed from the Users page.

## Logging, metrics and tracing

| Tool | URL | Purpose |
|---|---|---|
| Grafana | http://localhost:3000 | Dashboards, and log search through the "OpenSearch Logs" data source |
| Prometheus | http://localhost:9090 | Metrics scraped from every service's `/actuator/prometheus` |
| OpenSearch Dashboards | http://localhost:5601 | Log search (index pattern `eshop-logs-*`) |
| Kafka UI | http://localhost:8180 | Browse Kafka topics and brokers |

How logs flow: every service writes structured JSON to `logs/<service>.log`; Filebeat ships those files to OpenSearch; Grafana and OpenSearch Dashboards read from there.

Every request carries an `X-Correlation-ID`. The service filters and the method-level logging aspect tag each log line with it, so one ID follows a request across services. To follow an order: the cart page shows a trace ID in the success popup, and in Grafana Explore (data source "OpenSearch Logs", Logs view, wide enough time range) search:

```text
correlationId:"<trace id>"
```

or on the command line:

```powershell
Select-String -Path .\logs\*.log -Pattern "<trace id>" -SimpleMatch
```

This is log correlation across services, not span-level tracing. Method logging prints only numeric, boolean, enum and UUID arguments, never strings or request bodies, so passwords and tokens stay out of the logs.

## Repository layout

```text
my-e-shopping/
├── api-gateway/ auth-service/ customer-service/ order-service/ inventory-service/
├── cart-service/ payment-service/ picking-service/ shipment-service/ delivery-service/
├── invoice-service/ return-refund-service/ notification-service/ ...   (one folder per service)
├── frontend/angular-app/        Angular storefront and admin pages
├── dashboard/                   PowerShell ops dashboard (live and static)
├── infrastructure/
│   ├── docker/                  docker-compose for databases, Kafka, monitoring, logging
│   ├── prometheus/  grafana/  filebeat/
│   └── kafka/ postgres/ redis/ kubernetes/
├── logs/                        runtime logs (git-ignored)
└── docs/                        architecture, microservices, Kafka, security, deployment
```

## Security notes and known limitations

- **Development credentials.** The database, Grafana and OpenSearch passwords and the JWT signing secret are local-development defaults kept in `infrastructure/docker/docker-compose.yml`, the services' `application.*` files and the dashboard data files. Do not reuse them anywhere real. Before publishing the repository or deploying, move them to environment variables or a secrets store and rotate them.
- **Server-side authorization covers only the user-administration endpoints** (`/api/v1/auth/admin/**`, which require an admin token). Other admin actions, such as adding SKUs or reading customer totals, are hidden from normal users in the UI but the underlying service endpoints are not authenticated. Adding token validation at the gateway is the next step.
- Login tokens expire after one hour; the app signs you out when that happens.
- Stock stays reserved until an order is cancelled or returned; it is not permanently deducted on shipment.
- Delivery is simulated: fulfilment moves an order to `DELIVERED` within seconds, so the "cancel before delivery" window is very short. Add a delay before the delivery step if you want to demo cancelling. The 2-day return window is measured from the order's last update once it is `DELIVERED`.
- Orders placed before the Kafka consumer existed stay in `ORDERED`; nothing reprocesses them.
- Cancel, refund and invoice endpoints are not authenticated server-side, so anyone who can reach the gateway can call them for any order id.
- Kubernetes manifests, AWS resources and production hardening are still follow-up work.

## More detail

See the `docs/` folder for the architecture, microservice list, Kafka design, state machines, security and deployment notes.
