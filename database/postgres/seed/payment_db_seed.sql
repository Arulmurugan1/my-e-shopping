-- Sample payments for payment_db. order_id/customer_id match postgres/seed/order_db_seed.sql
-- (order 5005 has no payment yet - it's still PLACED)
-- Run after postgres/payment_db.sql

INSERT INTO payments (id, order_id, customer_id, amount, currency, idempotency_key, status, created_at, updated_at) VALUES
(1, 5001, 1001, 16.48, 'USD', 'ORD-5001-PAY-1', 'SUCCESS',  TIMESTAMP '2026-09-02 09:12:00', TIMESTAMP '2026-09-02 09:12:05'),
(2, 5002, 1002, 14.70, 'USD', 'ORD-5002-PAY-1', 'SUCCESS',  TIMESTAMP '2026-09-06 11:22:00', TIMESTAMP '2026-09-06 11:22:04'),
(3, 5003, 1001, 22.49, 'USD', 'ORD-5003-PAY-1', 'SUCCESS',  TIMESTAMP '2026-09-10 14:06:00', TIMESTAMP '2026-09-10 14:06:03'),
(4, 5004, 1003, 19.50, 'USD', 'ORD-5004-PAY-1', 'REFUNDED', TIMESTAMP '2026-09-04 10:02:00', TIMESTAMP '2026-09-04 15:30:00');

SELECT setval(pg_get_serial_sequence('payments', 'id'), (SELECT MAX(id) FROM payments));
