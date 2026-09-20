-- Sample return request for return_refund_db. order_id/customer_id match
-- postgres/seed/order_db_seed.sql (order 5001, customer 1001, partial return of the loaf)
-- Run after mysql/return_refund_db.sql

INSERT INTO return_requests (id, order_id, customer_id, reason, amount, status, requested_at, completed_at) VALUES
(1, 5001, 1001, 'Loaf arrived stale.', 6.50, 'APPROVED', '2026-09-06 10:00:00', '2026-09-07 12:00:00');
