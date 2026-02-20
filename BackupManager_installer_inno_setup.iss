; =========================================
; BackupManager - Inno Setup Installer
; =========================================

[Setup]
AppName=BackupManager
AppVersion=2.2.1
AppPublisher=Shard
AppPublisherURL=https://www.shardpc.it/
DefaultDirName={userdocs}\Shard\BackupManager
DisableDirPage=yes
DisableProgramGroupPage=no
PrivilegesRequired=lowest
OutputBaseFilename=BackupManager_v2.2.1_Setup
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
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs

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
function InitializeSetup(): Boolean;
begin
  Result := True;
end;
