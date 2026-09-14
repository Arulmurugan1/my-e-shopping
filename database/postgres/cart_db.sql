-- cart-service (database: cart_db)
-- Matches cart-service/.../entity/Cart.java and CartItem.java

CREATE TABLE IF NOT EXISTS carts (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
    id           BIGSERIAL PRIMARY KEY,
    cart_id      BIGINT NOT NULL REFERENCES carts(id),
    product_id   BIGINT NOT NULL,
    sku          VARCHAR(80) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price   DOUBLE PRECISION NOT NULL,
    quantity     INTEGER NOT NULL,
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id)
);
