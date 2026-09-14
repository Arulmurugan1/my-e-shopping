-- Sample picking tasks for picking_db. order_id matches postgres/seed/order_db_seed.sql
-- Run after postgres/picking_db.sql

INSERT INTO picking_tasks (id, order_id, item_count, status, created_at, updated_at) VALUES
(1, 5001, 2, 'COMPLETED',   TIMESTAMP '2026-09-02 09:15:00', TIMESTAMP '2026-09-03 08:00:00'),
(2, 5002, 2, 'COMPLETED',   TIMESTAMP '2026-09-06 11:25:00', TIMESTAMP '2026-09-07 09:00:00'),
(3, 5003, 2, 'IN_PROGRESS', TIMESTAMP '2026-09-10 14:10:00', TIMESTAMP '2026-09-10 14:10:00'),
(4, 5004, 1, 'CANCELLED',   TIMESTAMP '2026-09-04 10:05:00', TIMESTAMP '2026-09-04 15:30:00');

SELECT setval(pg_get_serial_sequence('picking_tasks', 'id'), (SELECT MAX(id) FROM picking_tasks));
