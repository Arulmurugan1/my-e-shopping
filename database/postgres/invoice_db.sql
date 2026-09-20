-- invoice-service (database: invoice_db)
-- Matches invoice-service/.../entity/Invoice.java

CREATE TABLE IF NOT EXISTS invoices (
    id              BIGSERIAL PRIMARY KEY,
    invoice_number  VARCHAR(40) NOT NULL UNIQUE,
    order_id        BIGINT NOT NULL UNIQUE,
    payment_id      BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    amount          DOUBLE PRECISION NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    issued_at       TIMESTAMP NOT NULL
);
