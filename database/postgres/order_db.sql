-- order-service (database: order_db)
-- Matches order-service/.../entity/Order.java and OrderLine.java

CREATE TABLE IF NOT EXISTS orders (
    id                BIGSERIAL PRIMARY KEY,
    customer_id       BIGINT NOT NULL,
    shipping_address  VARCHAR(500) NOT NULL,
    status            VARCHAR(30) NOT NULL,
    total_amount      DOUBLE PRECISION NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS order_lines (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT NOT NULL REFERENCES orders(id),
    product_id   BIGINT NOT NULL,
    sku          VARCHAR(80) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price   DOUBLE PRECISION NOT NULL,
    quantity     INTEGER NOT NULL
);
