-- Sample audit records for audit-service (Oracle XE, schema: system).
-- actor values reference the seeded auth users / customers from the other seeds.
-- NOTE: id is RAW(16) on the live table (see oracle/seed/auth_service_seed.sql note
-- about Hibernate's ddl-auto=update reconciling VARCHAR2(36) -> RAW(16)); hence
-- HEXTORAW() below instead of a plain string literal.
-- Run after oracle/audit_service.sql

INSERT INTO audit_records (id, event_type, actor, "timestamp", payload) VALUES
(HEXTORAW('CCCC3333CCCC3333CCCC3333CCCC3001'), 'USER_REGISTERED', 'alice.johnson@example.com', TIMESTAMP '2026-08-20 09:00:00', '{"userId":"AAAA1111-AAAA-1111-AAAA-1111AAAA1001"}');

INSERT INTO audit_records (id, event_type, actor, "timestamp", payload) VALUES
(HEXTORAW('CCCC3333CCCC3333CCCC3333CCCC3002'), 'ORDER_CREATED',    'customer:1001',            TIMESTAMP '2026-09-02 09:10:01', '{"orderId":5001,"totalAmount":16.48}');

INSERT INTO audit_records (id, event_type, actor, "timestamp", payload) VALUES
(HEXTORAW('CCCC3333CCCC3333CCCC3333CCCC3003'), 'PAYMENT_CAPTURED', 'payment-service',          TIMESTAMP '2026-09-02 09:12:05', '{"orderId":5001,"paymentId":1}');

INSERT INTO audit_records (id, event_type, actor, "timestamp", payload) VALUES
(HEXTORAW('CCCC3333CCCC3333CCCC3333CCCC3004'), 'ORDER_CANCELLED',  'customer:1003',            TIMESTAMP '2026-09-04 15:30:00', '{"orderId":5004,"reason":"customer request"}');

INSERT INTO audit_records (id, event_type, actor, "timestamp", payload) VALUES
(HEXTORAW('CCCC3333CCCC3333CCCC3333CCCC3005'), 'RETURN_APPROVED',  'return-refund-service',    TIMESTAMP '2026-09-07 12:00:00', '{"orderId":5001,"returnRequestId":1,"amount":6.50}');

COMMIT;
