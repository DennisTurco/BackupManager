-- Migration 006: schema integrity fixes via table recreation (SQLite limitation).
-- Wrapped in BEGIN/COMMIT directly in SQL to avoid SQLite-JDBC internal
-- prepared-statement invalidation that occurs when using setAutoCommit(false) + DDL.
--
--  1. BackupConfigurations: add CHECK(MaxToKeep >= 1)
--  2. BackupRequests FK:    ON DELETE CASCADE → ON DELETE RESTRICT
--  3. Subscriptions:        CreationType column type INTEGER → TEXT
--
-- Order: BackupConfigurations first (BackupRequests FK references it),
--        then BackupRequests, then Subscriptions (independent).
-- Foreign keys MUST be off while tables are recreated: with them on, DROP TABLE runs an implicit
-- DELETE that fires the old ON DELETE CASCADE and wipes every BackupRequests row before it is copied.
-- PRAGMA foreign_keys is a no-op inside a transaction, so it is set before BEGIN and restored after COMMIT.
-- (SQLite "Making Other Kinds Of Table Schema Changes" procedure.)

PRAGMA foreign_keys = OFF;

BEGIN;

-- ── 1. BackupConfigurations ──────────────────────────────────────────────────
CREATE TABLE "BackupConfigurations_v2" (
    "BackupId" INTEGER PRIMARY KEY AUTOINCREMENT,
    "BackupName" TEXT NOT NULL UNIQUE,
    "TargetPath" TEXT NOT NULL,
    "DestinationPath" TEXT NOT NULL,
    "LastBackupDate" INTEGER,
    "Automatic" INTEGER NOT NULL DEFAULT 0 CHECK("Automatic" IN (0, 1)),
    "NextBackupDate" INTEGER,
    "TimeIntervalBackup" TEXT,
    "CreationDate" INTEGER NOT NULL,
    "LastUpdateDate" INTEGER NOT NULL,
    "BackupCount" INTEGER NOT NULL DEFAULT 0,
    "MaxToKeep" INTEGER NOT NULL DEFAULT 1 CHECK("MaxToKeep" >= 1),
    "Notes" TEXT,
    "DeletedAt" INTEGER DEFAULT NULL
);
INSERT INTO "BackupConfigurations_v2"
    SELECT BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic,
           NextBackupDate, TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount,
           MAX(MaxToKeep, 1), Notes, DeletedAt
    FROM "BackupConfigurations";
DROP TABLE "BackupConfigurations";
ALTER TABLE "BackupConfigurations_v2" RENAME TO "BackupConfigurations";

-- ── 2. BackupRequests ────────────────────────────────────────────────────────
CREATE TABLE "BackupRequests_v2" (
    "BackupRequestId" INTEGER PRIMARY KEY AUTOINCREMENT,
    "BackupConfigurationId" INTEGER NOT NULL,
    "StartedDate" INTEGER NOT NULL,
    "CompletionDate" INTEGER NULL,
    "Status" INTEGER NOT NULL,
    "Progress" INTEGER DEFAULT 0 CHECK(Progress BETWEEN 0 AND 100),
    "TriggeredBy" INTEGER,
    "DurationMs" INTEGER,
    "OutputPath" TEXT,
    "UnzippedTargetSize" INTEGER NOT NULL DEFAULT 0,
    "ZippedTargetSize" INTEGER,
    "FilesCount" INTEGER DEFAULT NULL,
    "ErrorMessage" TEXT DEFAULT NULL,
    FOREIGN KEY("BackupConfigurationId") REFERENCES "BackupConfigurations"("BackupId") ON DELETE RESTRICT
);
INSERT INTO "BackupRequests_v2"
    SELECT BackupRequestId, BackupConfigurationId, StartedDate, CompletionDate, Status,
           Progress, TriggeredBy, DurationMs, OutputPath, UnzippedTargetSize, ZippedTargetSize,
           FilesCount, ErrorMessage
    FROM "BackupRequests";
DROP TABLE "BackupRequests";
ALTER TABLE "BackupRequests_v2" RENAME TO "BackupRequests";
CREATE INDEX IF NOT EXISTS idx_backup_status_started ON BackupRequests(Status, StartedDate DESC);
CREATE INDEX IF NOT EXISTS idx_backup_config_id ON BackupRequests(BackupConfigurationId);

-- ── 3. Subscriptions ─────────────────────────────────────────────────────────
CREATE TABLE "Subscriptions_v2" (
    "SubscriptionId" INTEGER PRIMARY KEY AUTOINCREMENT,
    "InsertDate" INTEGER NOT NULL,
    "StartDate" INTEGER NOT NULL UNIQUE,
    "EndDate" INTEGER NOT NULL,
    "CreationType" TEXT NOT NULL,
    CHECK("StartDate" <= "EndDate")
);
INSERT INTO "Subscriptions_v2"
    SELECT SubscriptionId, InsertDate, StartDate, EndDate, CAST(CreationType AS TEXT)
    FROM "Subscriptions";
DROP VIEW IF EXISTS v_Subscriptions;
DROP TABLE "Subscriptions";
ALTER TABLE "Subscriptions_v2" RENAME TO "Subscriptions";
CREATE VIEW IF NOT EXISTS v_Subscriptions AS
SELECT
    SubscriptionId,
    datetime(InsertDate / 1000, 'unixepoch', 'localtime') AS InsertDate,
    datetime(StartDate / 1000, 'unixepoch', 'localtime') AS StartDate,
    datetime(EndDate / 1000, 'unixepoch', 'localtime') AS EndDate,
    CreationType
FROM Subscriptions;

INSERT OR IGNORE INTO SchemaVersion VALUES (6);

COMMIT;

PRAGMA foreign_keys = ON;
