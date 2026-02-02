--
-- File generated with SQLiteStudio v3.4.17 on lun feb 2 16:24:51 2026
--
-- Text encoding used: System
--
PRAGMA foreign_keys = off;

-- Table: BackupConfigurations
CREATE TABLE IF NOT EXISTS "BackupConfigurations" (
	"BackupId"	INTEGER,
	"BackupName"	TEXT NOT NULL UNIQUE,
	"TargetPath"	TEXT NOT NULL,
	"DestinationPath"	TEXT NOT NULL,
	"LastBackupDate"	TEXT,
	"Automatic"	INTEGER NOT NULL DEFAULT 0 CHECK("Automatic" IN (0, 1)),
	"NextBackupDate"	TEXT,
	"TimeIntervalBackup"	TEXT,
	"CreationDate"	TEXT NOT NULL,
	"LastUpdateDate"	TEXT NOT NULL,
	"BackupCount"	INTEGER NOT NULL DEFAULT 0,
	"MaxToKeep"	INTEGER NOT NULL DEFAULT 1,
	"Notes"	TEXT,
	PRIMARY KEY("BackupId" AUTOINCREMENT)
);

-- Table: Backups
CREATE TABLE IF NOT EXISTS "Backups" (
	"BackupRequestId"	INTEGER,
	"BackupConfigurationId"	INTEGER NOT NULL,
	"StartedDate"	TEXT NOT NULL,
	"CompletionDate"	TEXT NOT NULL,
	"Status"	INTEGER NOT NULL DEFAULT 0,
	"TargetSize"	REAL NOT NULL DEFAULT 0,
	PRIMARY KEY("BackupRequestId" AUTOINCREMENT),
	FOREIGN KEY("BackupConfigurationId") REFERENCES "BackupConfigurations"("BackupId")
);

-- Table: Preferences
CREATE TABLE IF NOT EXISTS "Preferences" (
	"Code"	TEXT NOT NULL UNIQUE,
	"Value"	TEXT NOT NULL,
	PRIMARY KEY("Code")
);

-- Table: Users
CREATE TABLE IF NOT EXISTS "Users" (
	"UserId"	INTEGER,
	"Name"	TEXT NOT NULL,
	"Surname"	TEXT NOT NULL,
	"Email"	TEXT NOT NULL,
	"Language"	TEXT NOT NULL,
	"InsertDate"	TEXT NOT NULL,
	PRIMARY KEY("UserId" AUTOINCREMENT)
);

PRAGMA foreign_keys = on;
