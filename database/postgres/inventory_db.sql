-- inventory-service (database: inventory_db)
-- Matches inventory-service/.../entity/Product.java
-- NOTE: database/README.md previously said inventory-service used MongoDB; that is
-- stale. The service's application.properties points at Postgres (inventory_db) via
-- spring-datasource/JPA, so this DDL was added to match the other services here.

CREATE TABLE IF NOT EXISTS products (
    id                 BIGSERIAL PRIMARY KEY,
    sku                VARCHAR(80) NOT NULL UNIQUE,
    name               VARCHAR(200) NOT NULL,
    description        VARCHAR(1000),
    price              DOUBLE PRECISION NOT NULL,
    stock_quantity     INTEGER NOT NULL,
    reserved_quantity  INTEGER NOT NULL,
    is_active          BOOLEAN NOT NULL,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP
);
