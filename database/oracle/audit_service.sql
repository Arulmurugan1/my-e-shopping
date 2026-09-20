-- audit-service (Oracle XE, schema: system)
-- Matches audit-service/.../entity/AuditRecord.java
-- Note: id is a Java UUID; stored as VARCHAR2(36) - see auth_service.sql note.

CREATE TABLE audit_records (
    id          VARCHAR2(36) NOT NULL PRIMARY KEY,
    event_type  VARCHAR2(255),
    actor       VARCHAR2(255),
    "timestamp" TIMESTAMP,
    payload     CLOB
);
