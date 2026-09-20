-- return-refund-service (database: return_refund_db)
-- Matches return-refund-service/.../entity/ReturnRequestEntity.java

CREATE TABLE IF NOT EXISTS return_requests (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT NOT NULL,
    customer_id   BIGINT NOT NULL,
    reason        VARCHAR(500) NOT NULL,
    amount        DOUBLE NOT NULL,
    status        VARCHAR(30) NOT NULL,
    requested_at  DATETIME NOT NULL,
    completed_at  DATETIME
);
