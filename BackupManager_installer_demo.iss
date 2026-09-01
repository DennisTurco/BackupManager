; =========================================
; BackupManager - Inno Setup Installer (Demo)
; =========================================
; Subscription: ENABLED with 1 month free trial
; The installer writes a .demo-init marker file that the
; application reads on first launch to apply the demo SQL.
; =========================================

[Setup]
AppName=BackupManager
AppVersion=3.0.0
AppPublisher=Shard
AppPublisherURL=https://www.shardpc.it/
DefaultDirName={userdocs}\Shard\BackupManager
DisableDirPage=yes
DisableProgramGroupPage=no
PrivilegesRequired=lowest
OutputBaseFilename=BackupManager_v3.0.0_Demo_Setup
SetupIconFile=src\main\resources\res\img\logo.ico
SetupLogging=yes
Compression=lzma
SolidCompression=yes
WizardStyle=modern
UninstallDisplayName=Uninstall BackupManager
UninstallDisplayIcon={app}\BackupManager.exe

; immagini wizard
WizardImageFile=src\main\resources\res\img\shard.png
WizardSmallImageFile=src\main\resources\res\img\logo.png

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

; =========================================
; FILE INSTALLATI
; =========================================
[Files]
Source: "BackupManager.exe"; DestDir: "{app}"
Source: "README.md"; DestDir: "{app}"
Source: "config.enc"; DestDir: "{app}"

Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs
Source: "src\main\resources\*"; DestDir: "{app}\src\main\resources"; Flags: recursesubdirs

; =========================================
; AVVIO AUTOMATICO (PER-UTENTE)
; =========================================
[Registry]
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
  ValueType: string; ValueName: "BackupManager"; \
  ValueData: """{app}\BackupManager.exe"" --background"; \
  Flags: uninsdeletevalue

; =========================================
; POST-INSTALL
; =========================================
[Run]
Filename: "{app}\BackupManager.exe"; Parameters: "--background"; Flags: nowait postinstall

; =========================================
; COLLEGAMENTI
; =========================================
[Icons]
Name: "{userdesktop}\BackupManager"; Filename: "{app}\BackupManager.exe"
Name: "{userprograms}\BackupManager\BackupManager"; Filename: "{app}\BackupManager.exe"

; =========================================
; CODICE
; =========================================
[Code]
// Write the demo marker file to the database directory.
// ProductionDatabaseInitializer reads it on first run, applies
// 003_enable_demo_version.sql, then deletes the marker automatically.
procedure CreateDemoMarker();
var
  DbDir: String;
  MarkerPath: String;
begin
  DbDir := ExpandConstant('{userdocs}\Shard\data');
  ForceDirectories(DbDir);
  MarkerPath := DbDir + '\.demo-init';
  SaveStringToFile(MarkerPath, 'demo', False);
end;

function InitializeSetup(): Boolean;
begin
  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
    CreateDemoMarker();
end;
