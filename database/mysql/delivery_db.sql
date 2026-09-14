-- delivery-service (database: delivery_db)
-- Matches delivery-service/.../entity/Delivery.java

CREATE TABLE IF NOT EXISTS deliveries (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id     BIGINT NOT NULL UNIQUE,
    recipient_name  VARCHAR(160) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    created_at      DATETIME NOT NULL,
    delivered_at    DATETIME
);
