-- customer-service (database: customer_db)
-- Matches customer-service/.../entity/CustomerAddress.java and CustomerProfile.java

CREATE TABLE IF NOT EXISTS customer_addresses (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT NOT NULL,
    address_type VARCHAR(50) NOT NULL,
    street       VARCHAR(200) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100) NOT NULL,
    postal_code  VARCHAR(20) NOT NULL,
    country      VARCHAR(100) NOT NULL,
    is_primary   BOOLEAN NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer_profiles (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP
);
