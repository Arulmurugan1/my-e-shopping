-- Sample users for auth-service (Oracle XE, schema: system).
-- All three seeded users share the password "Passw0rd123" (hashed with the same
-- BCryptPasswordEncoder auth-service uses, so you can log in with it directly).
-- NOTE: oracle/auth_service.sql's DDL comment describes id as VARCHAR2(36), but
-- Hibernate's ddl-auto=update has actually reconciled it to RAW(16) (its default
-- JDBC mapping for a Java UUID field) on the live table - hence the HEXTORAW() here
-- instead of a plain string literal. If you run this against a fresh table created
-- strictly from the DDL script (VARCHAR2(36)) before the service ever starts, use
-- plain dashed UUID string literals instead.
-- user ids here are unrelated to the numeric customer_id (1001-1003) used in the
-- Postgres/MySQL seeds - customer_profiles.user_id in customer_db is what actually
-- links a login to a customer record, and currently has to be set by hand/registration
-- flow since auth-service's id is a UUID.
-- Run after oracle/auth_service.sql

INSERT INTO users (id, email, password, role, date_of_birth, gender, is_active, created_at, updated_at) VALUES
(HEXTORAW('AAAA1111AAAA1111AAAA1111AAAA1001'), 'alice.johnson@example.com', '$2a$10$hF8p8g3CBKM9/zdDpnX48O0vwMoux81BrxHxrpf1.UrtRqDNgq5IS', 'CUSTOMER', '1990-04-12', 'FEMALE', 1, TIMESTAMP '2026-08-20 09:00:00', TIMESTAMP '2026-08-20 09:00:00');

INSERT INTO users (id, email, password, role, date_of_birth, gender, is_active, created_at, updated_at) VALUES
(HEXTORAW('AAAA1111AAAA1111AAAA1111AAAA1002'), 'brian.lee@example.com', '$2a$10$hF8p8g3CBKM9/zdDpnX48O0vwMoux81BrxHxrpf1.UrtRqDNgq5IS', 'CUSTOMER', '1988-11-02', 'MALE', 1, TIMESTAMP '2026-08-21 10:15:00', TIMESTAMP '2026-08-21 10:15:00');

INSERT INTO users (id, email, password, role, date_of_birth, gender, is_active, created_at, updated_at) VALUES
(HEXTORAW('AAAA1111AAAA1111AAAA1111AAAA1003'), 'priya.patel@example.com', '$2a$10$hF8p8g3CBKM9/zdDpnX48O0vwMoux81BrxHxrpf1.UrtRqDNgq5IS', 'CUSTOMER', '1995-07-23', 'FEMALE', 1, TIMESTAMP '2026-08-22 11:30:00', TIMESTAMP '2026-08-22 11:30:00');

INSERT INTO users (id, email, password, role, date_of_birth, gender, is_active, created_at, updated_at) VALUES
(HEXTORAW('AAAA1111AAAA1111AAAA1111AAAA1000'), 'admin@myeshopping.com', '$2a$10$hF8p8g3CBKM9/zdDpnX48O0vwMoux81BrxHxrpf1.UrtRqDNgq5IS', 'ADMIN', NULL, 'OTHER', 1, TIMESTAMP '2026-08-15 08:00:00', TIMESTAMP '2026-08-15 08:00:00');

COMMIT;
