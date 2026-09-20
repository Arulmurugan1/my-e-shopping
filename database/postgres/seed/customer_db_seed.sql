-- Sample customer profiles/addresses for customer_db
-- customer_id / user_id values (1001-1003) are shared across cart_db, order_db,
-- payment_db, picking_db and invoice_db seeds to tell one consistent story.
-- Run after postgres/customer_db.sql

INSERT INTO customer_profiles (id, user_id, first_name, last_name, phone_number, created_at, updated_at) VALUES
(1, 1001, 'Alice', 'Johnson', '+1-555-0101', TIMESTAMP '2026-08-20 09:00:00', TIMESTAMP '2026-08-20 09:00:00'),
(2, 1002, 'Brian', 'Lee',     '+1-555-0102', TIMESTAMP '2026-08-21 10:15:00', TIMESTAMP '2026-08-21 10:15:00'),
(3, 1003, 'Priya', 'Patel',   '+1-555-0103', TIMESTAMP '2026-08-22 11:30:00', TIMESTAMP '2026-08-22 11:30:00');

INSERT INTO customer_addresses (id, customer_id, address_type, street, city, state, postal_code, country, is_primary, created_at, updated_at) VALUES
(1, 1001, 'HOME', '123 Market Street',     'San Francisco', 'CA', '94103', 'USA', true,  TIMESTAMP '2026-08-20 09:05:00', TIMESTAMP '2026-08-20 09:05:00'),
(2, 1001, 'WORK',  '400 Bryant Street',     'San Francisco', 'CA', '94107', 'USA', false, TIMESTAMP '2026-08-25 13:00:00', TIMESTAMP '2026-08-25 13:00:00'),
(3, 1002, 'HOME', '77 Riverside Drive',     'Austin',        'TX', '73301', 'USA', true,  TIMESTAMP '2026-08-21 10:20:00', TIMESTAMP '2026-08-21 10:20:00'),
(4, 1003, 'HOME', '9 Maple Avenue',         'Seattle',       'WA', '98101', 'USA', true,  TIMESTAMP '2026-08-22 11:35:00', TIMESTAMP '2026-08-22 11:35:00');

SELECT setval(pg_get_serial_sequence('customer_profiles', 'id'), (SELECT MAX(id) FROM customer_profiles));
SELECT setval(pg_get_serial_sequence('customer_addresses', 'id'), (SELECT MAX(id) FROM customer_addresses));
