; =============================================================================
; BackupManager - Inno Setup Installer (Demo)
; =============================================================================
; Subscription: ENABLED with 1 month free trial.
; On a machine with no BackupManager database yet, the installer writes a
; .demo-init marker next to where the database will be created; on first launch
; ProductionDatabaseInitializer applies 003_enable_demo_version.sql and deletes it.
; An existing database is left untouched.
; All shared settings, prerequisites and build steps are in BackupManager_common.iss.
; =============================================================================

#define DemoEdition
#define EditionSuffix "_Demo"
#define EditionLabel  " (Demo)"

#include "BackupManager_common.iss"
