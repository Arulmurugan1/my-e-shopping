-- Sample active cart for cart_db (customer 1003 is mid-browsing, hasn't checked out)
-- Product ids match postgres/seed/inventory_db_seed.sql
-- Run after postgres/cart_db.sql

INSERT INTO carts (id, customer_id, status, created_at, updated_at) VALUES
(1, 1003, 'ACTIVE', TIMESTAMP '2026-09-10 16:00:00', TIMESTAMP '2026-09-10 16:04:00');

INSERT INTO cart_items (id, cart_id, product_id, sku, product_name, unit_price, quantity) VALUES
(1, 1, 6, 'COF-006', 'Ground Coffee (500g)', 12.50, 1),
(2, 1, 7, 'CHS-007', 'Aged Cheddar Cheese (250g)', 7.25, 2);

SELECT setval(pg_get_serial_sequence('carts', 'id'), (SELECT MAX(id) FROM carts));
SELECT setval(pg_get_serial_sequence('cart_items', 'id'), (SELECT MAX(id) FROM cart_items));
