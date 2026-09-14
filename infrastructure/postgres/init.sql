-- Creates one database per PostgreSQL-backed microservice.
-- Runs automatically on first container startup (docker-entrypoint-initdb.d).
-- Tables/schema within each database are created by each service's own
-- Hibernate ddl-auto=update on startup, so only the databases need to exist here.
CREATE DATABASE customer_db;
CREATE DATABASE cart_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE picking_db;
CREATE DATABASE invoice_db;
CREATE DATABASE inventory_db;
