-- Migration 004: add index on BackupRequests.BackupConfigurationId
-- Improves query performance when fetching backup history for a specific configuration.
-- Safe to run on existing databases (IF NOT EXISTS guard).
CREATE INDEX IF NOT EXISTS idx_backup_config_id
ON BackupRequests(BackupConfigurationId);

INSERT OR IGNORE INTO SchemaVersion VALUES (4);
