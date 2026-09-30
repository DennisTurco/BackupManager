-- Migration 005: soft delete for BackupConfigurations
-- Adds DeletedAt column so configurations can be hidden without losing
-- their associated BackupRequests history (used in dashboard analytics).
--
-- Note: the ON DELETE CASCADE → RESTRICT change on BackupRequests.BackupConfigurationId
-- requires a full table recreation and is therefore NOT applied here for existing
-- databases. Since soft delete prevents hard deletes in normal usage, the existing
-- CASCADE behaviour is harmless on already-deployed databases.
ALTER TABLE BackupConfigurations ADD COLUMN DeletedAt INTEGER DEFAULT NULL;

INSERT OR IGNORE INTO SchemaVersion VALUES (5);
