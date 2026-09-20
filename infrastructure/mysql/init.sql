-- Creates one database per MySQL-backed microservice and grants the app user access.
-- Runs automatically on first container startup (docker-entrypoint-initdb.d).
-- Tables/schema within each database are created by each service's own
-- Hibernate ddl-auto=update on startup, so only the databases need to exist here.
CREATE DATABASE IF NOT EXISTS shipment_db;
CREATE DATABASE IF NOT EXISTS delivery_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS return_refund_db;

GRANT ALL PRIVILEGES ON shipment_db.* TO 'myeshopping'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'myeshopping'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'myeshopping'@'%';
GRANT ALL PRIVILEGES ON return_refund_db.* TO 'myeshopping'@'%';
FLUSH PRIVILEGES;
