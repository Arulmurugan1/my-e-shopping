# Database DDL scripts

One `.sql` file per service database, hand-derived from that service's JPA `@Entity`
classes (`database/postgres/`, `database/mysql/`, `database/oracle/`). File name matches
the database name.

| Folder | Service -> database | Table(s) |
|---|---|---|
| postgres/cart_db.sql | cart-service | carts, cart_items |
| postgres/order_db.sql | order-service | orders, order_lines |
| postgres/payment_db.sql | payment-service | payments |
| postgres/picking_db.sql | picking-service | picking_tasks |
| postgres/invoice_db.sql | invoice-service | invoices |
| postgres/customer_db.sql | customer-service | customer_addresses, customer_profiles |
| mysql/shipment_db.sql | shipment-service | shipments |
| mysql/delivery_db.sql | delivery-service | deliveries |
| mysql/return_refund_db.sql | return-refund-service | return_requests |
| mysql/notification_db.sql | notification-service | (none - no persistence yet) |
| oracle/auth_service.sql | auth-service | users |
| oracle/logging_service.sql | logging-service | log_entries |
| oracle/audit_service.sql | audit-service | audit_records |
| postgres/inventory_db.sql | inventory-service | products |

`api-gateway`, `cartonization-service`, `order-group-service`, `scheduler-service` have
no database.

## Seed data

Each folder has a `seed/` subdirectory with sample `INSERT` statements for that
database, e.g. `database/postgres/seed/order_db_seed.sql`. They tell one consistent
story across services: three customers (`customer_id`/`user_id` 1001-1003), five
orders (`order_id` 5001-5005) in different lifecycle states (delivered, shipped, paid,
cancelled, just-placed), with matching payments, picking tasks, invoices, shipments,
a delivery, a return request, an active cart, and correlated log/audit entries.

The three seeded auth users (`oracle/seed/auth_service_seed.sql`) all use the password
`Passw0rd123` (BCrypt-hashed with the same encoder auth-service uses), so you can log
in with them directly after seeding, e.g. `alice.johnson@example.com` / `Passw0rd123`.

Run DDL first, then the matching seed file, e.g.:
```powershell
docker exec -i docker-postgres-1 psql -U myeshopping -d order_db < database/postgres/order_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d order_db < database/postgres/seed/order_db_seed.sql
```

## Why these exist even though ddl-auto=update is set

Every JPA service has `spring.jpa.hibernate.ddl-auto=update`, so Hibernate normally
creates/updates these tables automatically the first time the service actually starts.
These scripts exist so tables can be created **before** a service is ever run (e.g. right
after `docker compose up`, for inspecting the schema in DBeaver, or for environments where
you don't want the app itself issuing DDL). They were applied once already to get the
currently-running containers into a working state.

If you run the actual Spring Boot service afterward, Hibernate's `ddl-auto=update` will
compare its expected schema to what's here and add anything missing - it won't drop or
break existing tables/columns.

## Re-applying manually

```powershell
# Postgres (one file per database)
docker exec -i docker-postgres-1 psql -U myeshopping -d cart_db     < database/postgres/cart_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d order_db    < database/postgres/order_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d payment_db  < database/postgres/payment_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d picking_db  < database/postgres/picking_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d invoice_db  < database/postgres/invoice_db.sql
docker exec -i docker-postgres-1 psql -U myeshopping -d customer_db < database/postgres/customer_db.sql

# MySQL
docker exec -i docker-mysql-1 mysql -u myeshopping -pmyeshopping shipment_db      < database/mysql/shipment_db.sql
docker exec -i docker-mysql-1 mysql -u myeshopping -pmyeshopping delivery_db      < database/mysql/delivery_db.sql
docker exec -i docker-mysql-1 mysql -u myeshopping -pmyeshopping return_refund_db < database/mysql/return_refund_db.sql

# Oracle (system schema, shared by auth/logging/audit-service)
docker exec -i docker-oracle-1 sqlplus -S system/myeshopping123@//localhost:1521/XE @database/oracle/auth_service.sql
docker exec -i docker-oracle-1 sqlplus -S system/myeshopping123@//localhost:1521/XE @database/oracle/logging_service.sql
docker exec -i docker-oracle-1 sqlplus -S system/myeshopping123@//localhost:1521/XE @database/oracle/audit_service.sql
```

Scripts use `CREATE TABLE IF NOT EXISTS` (Postgres/MySQL) so they're safe to re-run.
Oracle has no `IF NOT EXISTS` clause for `CREATE TABLE`; re-running against an already
existing table will error with `ORA-00955` - that's expected and harmless.
