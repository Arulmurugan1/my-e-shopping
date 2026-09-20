# DBeaver connections for My E-Shopping

One DBeaver connection **per service** (14 total) - the 4 services with no database
(api-gateway, cartonization-service, order-group-service, scheduler-service) and the
frontend are not included since there's nothing to connect to.

| Service | Type | Database | Port |
|---|---|---|---|
| auth-service | Oracle XE | XE (`system` schema) | 1521 |
| logging-service | Oracle XE | XE (`system` schema) | 1521 |
| audit-service | Oracle XE | XE (`system` schema) | 1521 |
| inventory-service | MongoDB | inventory_db | 27017 |
| cart-service | PostgreSQL | cart_db | 5432 |
| order-service | PostgreSQL | order_db | 5432 |
| payment-service | PostgreSQL | payment_db | 5432 |
| picking-service | PostgreSQL | picking_db | 5432 |
| invoice-service | PostgreSQL | invoice_db | 5432 |
| customer-service | PostgreSQL | customer_db | 5432 |
| shipment-service | MySQL | shipment_db | 3306 |
| delivery-service | MySQL | delivery_db | 3306 |
| return-refund-service | MySQL | return_refund_db | 3306 |
| notification-service | MySQL | notification_db | 3306 |

Note: auth/logging/audit-service technically point at the same Oracle schema, so those
three connections will show identical content - they're kept separate here only because
that's what was asked for.

## Option A - Import data-sources.json (fastest)

DBeaver stores each project's connections in a `data-sources.json` file. To load the ones
in this folder:

1. Open DBeaver -> **File > New > DBeaver > Project...** and create a new (or use an
   existing) project. Give it any name, e.g. `MyEShopping`.
2. **Close DBeaver completely.**
3. Find that project's folder on disk - by default:
   `%APPDATA%\DBeaverData\workspace6\<ProjectName>\.dbeaver\`
   (check via DBeaver's status bar / General preferences if unsure of the workspace path).
4. Copy `data-sources.json` from this folder into `<ProjectName>\.dbeaver\`, overwriting
   the existing (likely empty) one.
5. Reopen DBeaver. You should see all 14 connections listed above under a
   "My E-Shopping" folder in the Database Navigator.
6. Passwords were **not** included in the file (so nothing sensitive sits in a plain
   JSON file) - on first connect, DBeaver will prompt for each password once. Use the
   table below. Check "Save password" in that prompt so you're not asked again.

If your DBeaver version stores/reads this file differently and the import doesn't show
up, use Option B below instead - it always works, just takes longer for 14 connections.

## Option B - Create manually (always works)

**Database > New Database Connection** for each service, using these settings.
Everything not listed can be left at its default.

### Oracle (auth-service / logging-service / audit-service - repeat 3x with different names)
- Host: `localhost`  Port: `1521`
- Connect by **SID**, SID: `XE`
- Username: `system`  Password: `myeshopping123`

### MongoDB (inventory-service)
- Host: `localhost`  Port: `27017`  Database: `inventory_db`
- Username: `myeshopping`  Password: `myeshopping`
- Authentication Database: `admin`

### PostgreSQL (cart / order / payment / picking / invoice / customer-service)
- Host: `localhost`  Port: `5432`
- Username: `myeshopping`  Password: `myeshopping`
- Database: use the specific `*_db` name from the table above for each connection
  (cart_db, order_db, payment_db, picking_db, invoice_db, customer_db).

### MySQL (shipment / delivery / return-refund / notification-service)
- Host: `localhost`  Port: `3306`
- Username: `myeshopping`  Password: `myeshopping`
- Database: use the specific `*_db` name from the table above for each connection
  (shipment_db, delivery_db, return_refund_db, notification_db).

## Notes

- These are the same local dev credentials used by `infrastructure/docker/docker-compose.yml`
  and each service's `application.properties`/`application.yml` - not production secrets.
- Make sure `docker compose up -d` (from `infrastructure/docker/`) is running first, or
  every connection test will fail.
- If PostgreSQL gives `FATAL: database "..." does not exist`, the container's data volume
  was initialized before the per-service databases existed - see
  `infrastructure/postgres/init.sql` for how those databases are created, and create any
  missing ones manually with `CREATE DATABASE <name>;` via `docker exec` into the
  postgres container if the volume already existed before that file was fixed.
