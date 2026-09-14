-- picking-service (database: picking_db)
-- Matches picking-service/.../entity/PickingTask.java

CREATE TABLE IF NOT EXISTS picking_tasks (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    item_count INTEGER NOT NULL,
    status     VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
