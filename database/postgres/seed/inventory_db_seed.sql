-- Sample catalog for inventory_db.products
-- Run after postgres/inventory_db.sql

INSERT INTO products (id, sku, name, description, price, stock_quantity, reserved_quantity, is_active, created_at, updated_at) VALUES
(1, 'APL-001', 'Honeycrisp Apples (1kg)',        'Crisp and sweet, hand-picked.',            4.99, 50, 0, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-01 08:00:00'),
(2, 'BRD-002', 'Sourdough Loaf',                  'Fresh-baked daily.',                       6.50, 30, 3, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-05 09:15:00'),
(3, 'MLK-003', 'Whole Milk (1L)',                 'Locally sourced dairy.',                   3.20, 80, 2, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-06 10:30:00'),
(4, 'EGG-004', 'Free-range Eggs (12)',            'Grade A, free-range.',                     5.75, 60, 2, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-06 10:30:00'),
(5, 'CHK-005', 'Organic Chicken Breast (1kg)',    'Antibiotic-free, farm-raised.',            9.99, 40, 1, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-08 12:00:00'),
(6, 'COF-006', 'Ground Coffee (500g)',            'Medium roast Arabica blend.',             12.50, 25, 1, true, TIMESTAMP '2026-09-01 08:00:00', TIMESTAMP '2026-09-08 12:00:00'),
(7, 'CHS-007', 'Aged Cheddar Cheese (250g)',      'Sharp, 12-month aged.',                    7.25, 20, 0, true, TIMESTAMP '2026-09-02 08:00:00', TIMESTAMP '2026-09-02 08:00:00'),
(8, 'TOM-008', 'Vine Tomatoes (500g)',            'Ripened on the vine.',                     2.85,  0, 0, false, TIMESTAMP '2026-09-02 08:00:00', TIMESTAMP '2026-09-10 14:00:00');

SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));
