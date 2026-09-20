-- logging-service (Oracle XE, schema: system)
-- Matches logging-service/.../entity/LogEntry.java
-- Note: id is a Java UUID; stored as VARCHAR2(36) - see auth_service.sql note.

CREATE TABLE log_entries (
    id          VARCHAR2(36) NOT NULL PRIMARY KEY,
    "source"    VARCHAR2(255),
    "level"     VARCHAR2(255),
    message     VARCHAR2(255),
    "timestamp" TIMESTAMP,
    payload     CLOB
);
