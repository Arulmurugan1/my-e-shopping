-- shipment-service (database: shipment_db)
-- Matches shipment-service/.../entity/Shipment.java

CREATE TABLE IF NOT EXISTS shipments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id         BIGINT NOT NULL UNIQUE,
    tracking_number  VARCHAR(60) NOT NULL UNIQUE,
    carrier          VARCHAR(100) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    created_at       DATETIME NOT NULL,
    completed_at     DATETIME
);
