-- Sample log entries for logging-service (Oracle XE, schema: system).
-- Correlates loosely with the order lifecycle in the other seeds (orders 5001-5005).
-- NOTE: id is RAW(16) on the live table (see oracle/seed/auth_service_seed.sql note
-- about Hibernate's ddl-auto=update reconciling VARCHAR2(36) -> RAW(16)); hence
-- HEXTORAW() below instead of a plain string literal.
-- Run after oracle/logging_service.sql

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2001'), 'order-service',   'INFO',  'Order created',              TIMESTAMP '2026-09-02 09:10:01', '{"orderId":5001,"customerId":1001}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2002'), 'payment-service', 'INFO',  'Payment captured',            TIMESTAMP '2026-09-02 09:12:05', '{"orderId":5001,"paymentId":1,"amount":16.48}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2003'), 'shipment-service','INFO',  'Shipment dispatched',         TIMESTAMP '2026-09-03 08:05:00', '{"orderId":5001,"trackingNumber":"TRK-US-100001"}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2004'), 'delivery-service','INFO',  'Delivery completed',          TIMESTAMP '2026-09-05 17:40:00', '{"orderId":5001,"recipient":"Alice Johnson"}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2005'), 'payment-service', 'ERROR', 'Payment gateway timeout, retrying', TIMESTAMP '2026-09-06 11:21:40', '{"orderId":5002,"attempt":1}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2006'), 'payment-service', 'INFO',  'Payment refunded',            TIMESTAMP '2026-09-04 15:30:00', '{"orderId":5004,"paymentId":4,"amount":19.50}');

INSERT INTO log_entries (id, "source", "level", message, "timestamp", payload) VALUES
(HEXTORAW('BBBB2222BBBB2222BBBB2222BBBB2007'), 'inventory-service','WARN', 'Product deactivated due to zero stock', TIMESTAMP '2026-09-10 14:00:00', '{"productId":8,"sku":"TOM-008"}');

COMMIT;
