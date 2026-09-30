--
-- File generated with SQLiteStudio v3.4.17 on lun feb 2 16:24:51 2026
--
-- Text encoding used: System
--
PRAGMA foreign_keys = off;

-- Table: BackupConfigurations
CREATE TABLE IF NOT EXISTS "BackupConfigurations" (
	"BackupId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"BackupName" TEXT NOT NULL UNIQUE,
	"TargetPath" TEXT NOT NULL,
	"DestinationPath" TEXT NOT NULL,
	"LastBackupDate" INTEGER,
	"Automatic"	INTEGER NOT NULL DEFAULT 0 CHECK("Automatic" IN (0, 1)),
	"NextBackupDate" INTEGER,
	"TimeIntervalBackup" TEXT,
	"CreationDate" INTEGER NOT NULL,
	"LastUpdateDate" INTEGER NOT NULL,
	"BackupCount" INTEGER NOT NULL DEFAULT 0,
	"MaxToKeep"	INTEGER NOT NULL DEFAULT 1 CHECK("MaxToKeep" >= 1),
	"Notes"	TEXT,
	"DeletedAt" INTEGER DEFAULT NULL
);

-- Table: BackupRequests
-- Status: is a int for a enum because it is used a lot
CREATE TABLE IF NOT EXISTS "BackupRequests" (
	"BackupRequestId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"BackupConfigurationId"	INTEGER NOT NULL,
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
CREATE INDEX IF NOT EXISTS idx_backup_status_started
ON BackupRequests(Status, StartedDate DESC);
CREATE INDEX IF NOT EXISTS idx_backup_config_id
ON BackupRequests(BackupConfigurationId);

-- Table: Configurations
CREATE TABLE IF NOT EXISTS "Configurations" (
	"Code" TEXT PRIMARY KEY,
	"Value" TEXT NOT NULL
);

-- Table: SchemaVersion
CREATE TABLE IF NOT EXISTS "SchemaVersion" (
    "Version" INTEGER PRIMARY KEY
);

-- Table: Subscriptions
-- i don't want to bind this table to the user table because the subscription is global
CREATE TABLE IF NOT EXISTS "Subscriptions" (
	"SubscriptionId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"InsertDate" INTEGER NOT NULL,
	"StartDate" INTEGER NOT NULL UNIQUE,
	"EndDate" INTEGER NOT NULL,
	"CreationType" TEXT NOT NULL,
	CHECK("StartDate" <= "EndDate")
);

-- View: Subscriptions
CREATE VIEW IF NOT EXISTS v_Subscriptions AS
SELECT
    SubscriptionId,
    datetime(InsertDate / 1000, 'unixepoch', 'localtime') AS InsertDate,
    datetime(StartDate / 1000, 'unixepoch', 'localtime') AS StartDate,
    datetime(EndDate / 1000, 'unixepoch', 'localtime') AS EndDate,
	CreationType
FROM Subscriptions;


PRAGMA foreign_keys = on;
