-- payment-service (database: payment_db)
-- Matches payment-service/.../entity/Payment.java

CREATE TABLE IF NOT EXISTS payments (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT NOT NULL,
    customer_id      BIGINT NOT NULL,
    amount           DOUBLE PRECISION NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    idempotency_key  VARCHAR(200) NOT NULL UNIQUE,
    status           VARCHAR(30) NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);
