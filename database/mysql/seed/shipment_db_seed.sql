-- Sample shipments for shipment_db. order_id matches postgres/seed/order_db_seed.sql
-- Run after mysql/shipment_db.sql

INSERT INTO shipments (id, order_id, tracking_number, carrier, status, created_at, completed_at) VALUES
(1, 5001, 'TRK-US-100001', 'UPS',   'DELIVERED', '2026-09-03 08:05:00', '2026-09-05 17:40:00'),
(2, 5002, 'TRK-US-100002', 'FedEx', 'IN_TRANSIT', '2026-09-07 09:10:00', NULL);
