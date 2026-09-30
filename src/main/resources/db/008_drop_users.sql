-- Migration 008: drop the Users table.
-- It held the local "registration" shown on first launch, used only for the removed
-- email feature and for display. Subscriptions were never bound to a user, and nothing
-- references this table, so dropping it is safe with foreign keys on.
DROP TABLE IF EXISTS "Users";

INSERT OR IGNORE INTO SchemaVersion VALUES (8);
