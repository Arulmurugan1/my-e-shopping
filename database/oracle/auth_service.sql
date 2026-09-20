-- auth-service (Oracle XE, schema: system)
-- Matches auth-service/.../entity/UserEntity.java
-- Note: id is a Java UUID (GenerationType.UUID); stored here as VARCHAR2(36) for
-- readability/portability. Hibernate's ddl-auto=update will reconcile the exact
-- column type on first service startup if it prefers a different JDBC mapping.

CREATE TABLE users (
    id            VARCHAR2(36) NOT NULL PRIMARY KEY,
    email         VARCHAR2(255) NOT NULL UNIQUE,
    password      VARCHAR2(255) NOT NULL,
    role          VARCHAR2(255) NOT NULL,
    date_of_birth VARCHAR2(255),
    gender        VARCHAR2(255) NOT NULL,
    is_active     NUMBER(1) DEFAULT 1 NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP
);
