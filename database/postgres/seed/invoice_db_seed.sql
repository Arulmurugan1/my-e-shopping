-- Sample invoices for invoice_db. order_id/payment_id/customer_id match the
-- postgres/seed/order_db_seed.sql and postgres/seed/payment_db_seed.sql rows
-- (only orders that reached SUCCESS payment get an invoice; 5004 was refunded
-- before invoicing and 5005 hasn't paid yet, so neither has one here).
-- Run after postgres/invoice_db.sql

INSERT INTO invoices (id, invoice_number, order_id, payment_id, customer_id, amount, currency, issued_at) VALUES
(1, 'INV-2026-5001', 5001, 1, 1001, 16.48, 'USD', TIMESTAMP '2026-09-02 09:13:00'),
(2, 'INV-2026-5002', 5002, 2, 1002, 14.70, 'USD', TIMESTAMP '2026-09-06 11:23:00'),
(3, 'INV-2026-5003', 5003, 3, 1001, 22.49, 'USD', TIMESTAMP '2026-09-10 14:07:00');

SELECT setval(pg_get_serial_sequence('invoices', 'id'), (SELECT MAX(id) FROM invoices));
