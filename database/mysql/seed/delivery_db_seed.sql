-- Sample deliveries for delivery_db. shipment_id matches mysql/seed/shipment_db_seed.sql
-- (only the completed shipment, id 1, has a delivery record so far)
-- Run after mysql/delivery_db.sql

INSERT INTO deliveries (id, shipment_id, recipient_name, status, created_at, delivered_at) VALUES
(1, 1, 'Alice Johnson', 'DELIVERED', '2026-09-05 09:00:00', '2026-09-05 17:40:00');
