-- Sample orders for order_db.
-- order_id values (5001-5005) are reused as-is in payment_db, picking_db, invoice_db,
-- mysql/shipment_db, mysql/delivery_db and mysql/return_refund_db seeds.
-- Run after postgres/order_db.sql

INSERT INTO orders (id, customer_id, shipping_address, status, total_amount, created_at, updated_at) VALUES
(5001, 1001, '123 Market Street, San Francisco, CA 94103, USA', 'DELIVERED', 16.48, TIMESTAMP '2026-09-02 09:10:00', TIMESTAMP '2026-09-05 17:45:00'),
(5002, 1002, '77 Riverside Drive, Austin, TX 73301, USA',       'SHIPPED',   14.70, TIMESTAMP '2026-09-06 11:20:00', TIMESTAMP '2026-09-08 08:30:00'),
(5003, 1001, '123 Market Street, San Francisco, CA 94103, USA', 'PAID',      22.49, TIMESTAMP '2026-09-10 14:05:00', TIMESTAMP '2026-09-10 14:07:00'),
(5004, 1003, '9 Maple Avenue, Seattle, WA 98101, USA',          'CANCELLED', 19.50, TIMESTAMP '2026-09-04 10:00:00', TIMESTAMP '2026-09-04 15:30:00'),
(5005, 1002, '77 Riverside Drive, Austin, TX 73301, USA',       'PLACED',     6.40, TIMESTAMP '2026-09-12 09:00:00', TIMESTAMP '2026-09-12 09:00:00');

INSERT INTO order_lines (id, order_id, product_id, sku, product_name, unit_price, quantity) VALUES
(1, 5001, 1, 'APL-001', 'Honeycrisp Apples (1kg)', 4.99, 2),
(2, 5001, 2, 'BRD-002', 'Sourdough Loaf',          6.50, 1),
(3, 5002, 3, 'MLK-003', 'Whole Milk (1L)',         3.20, 1),
(4, 5002, 4, 'EGG-004', 'Free-range Eggs (12)',    5.75, 2),
(5, 5003, 6, 'COF-006', 'Ground Coffee (500g)',   12.50, 1),
(6, 5003, 5, 'CHK-005', 'Organic Chicken Breast (1kg)', 9.99, 1),
(7, 5004, 2, 'BRD-002', 'Sourdough Loaf',          6.50, 3),
(8, 5005, 3, 'MLK-003', 'Whole Milk (1L)',         3.20, 2);

SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
SELECT setval(pg_get_serial_sequence('order_lines', 'id'), (SELECT MAX(id) FROM order_lines));
