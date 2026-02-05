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
	"MaxToKeep"	INTEGER NOT NULL DEFAULT 1,
	"Notes"	TEXT
);

-- Table: Backups
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
	"UnzippedTargetSize" INTEGER NOT NULL DEFAULT 0,
	"ZippedTargetSize" INTEGER,
	"FilesCount" INTEGER DEFAULT NULL,
	"ErrorMessage" TEXT DEFAULT NULL,
	FOREIGN KEY("BackupConfigurationId") REFERENCES "BackupConfigurations"("BackupId") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_backup_status_started
ON BackupRequests(Status, StartedDate DESC);

-- Table: Preferences
CREATE TABLE IF NOT EXISTS "Preferences" (
	"Code" TEXT PRIMARY KEY,
	"Value" TEXT NOT NULL
);

-- Table: Users
CREATE TABLE IF NOT EXISTS "Users" (
	"UserId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"Name" TEXT NOT NULL,
	"Surname" TEXT NOT NULL,
	"Email"	TEXT NOT NULL UNIQUE,
	"Language" TEXT NOT NULL,
	"InsertDate" INTEGER NOT NULL
);

-- Table: SchemaVersion
CREATE TABLE IF NOT EXISTS "SchemaVersion" (
    "Version" INTEGER PRIMARY KEY
);

-- Table: Emails
-- Payload: could contains a json text with the error, backupId, context
CREATE TABLE IF NOT EXISTS "Emails" (
	"EmailId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"Type" INTEGER NOT NULL,
	"InsertDate" INTEGER NOT NULL,
	"AppVersion" TEXT NOT NULL,
	"Payload" TEXT
);
CREATE INDEX idx_emails_type_date
ON Emails(Type, InsertDate);


-- Table: Subscriptions
-- i don't want to bind this table to the user table because the subscription is global
CREATE TABLE IF NOT EXISTS "Subscriptions" (
	"SubscriptionId" INTEGER PRIMARY KEY AUTOINCREMENT,
	"InsertDate" INTEGER NOT NULL,
	"StartDate" INTEGER NOT NULL,
	"EndDate" INTEGER NOT NULL,
	CHECK("StartDate" <= "EndDate")
);

PRAGMA foreign_keys = on;
