-- Migration 007: drop the Emails table.
-- It only rate-limited the emails the app used to send (error reports, registration),
-- a feature that has been removed. Nothing references this table, so dropping it is
-- safe with foreign keys on.
DROP INDEX IF EXISTS idx_emails_type_date;
DROP TABLE IF EXISTS "Emails";

INSERT OR IGNORE INTO SchemaVersion VALUES (7);
