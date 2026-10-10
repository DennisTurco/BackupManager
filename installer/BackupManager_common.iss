; =============================================================================
; BackupManager - shared Inno Setup definitions
; =============================================================================
; Do not compile this file directly: compile one of the edition scripts, which
; set the edition macros and then #include this file.
;   - BackupManager_installer.iss       Standard (no subscription required)
;   - BackupManager_installer_demo.iss  Demo (subscription enabled, free trial)
;
; Prerequisites (from the repository root):
;   1. mvnw.cmd clean package -DskipTests        -> target\backupmanager-jar-with-dependencies.jar
;   2. cd ui && npm install && npm run dist:win   -> ui\release\win-unpacked\
;      (electron-builder bundles the backend JAR, jre\, src\main\resources\
;       and icon.ico under resources\)
;
; To compile: open an edition script in Inno Setup Compiler and press Ctrl+F9,
; or run:  ISCC.exe installer\BackupManager_installer.iss
; The installers are written to installer\Output\.
; =============================================================================

#ifndef EditionSuffix
  #error Compile BackupManager_installer.iss or BackupManager_installer_demo.iss, not this file
#endif

#define AppName      "BackupManager"
#define AppVersion   "3.0.0"
#define AppPublisher "Shard"
#define AppURL       "https://www.shardpc.it/"
#define AppSupportURL "https://github.com/DennisTurco/BackupManager"
#define AppExeName   "BackupManager.exe"
; Must match app.setAppUserModelId in ui/src/main/index.ts, or Windows drops the app's notifications
#define AppUserModelID "io.github.dennisturco.backupmanager"
#ifndef SourceDir
  #define SourceDir  "..\ui\release\win-unpacked"
#endif

; Same folder the backend uses for its database (DatabasePaths.getProductionDatabasePath)
#define DataDir      "{userdocs}\Shard\data"
#define DemoMarker   ".demo-init"

[Setup]
; Kept equal to the implicit AppId of the previous (Swing) installers, which had no AppId and
; therefore used AppName: this way the new installer upgrades existing installations in place.
; Both editions share it, so installing one over the other replaces it instead of duplicating it.
AppId={#AppName}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}{#EditionLabel}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppSupportURL={#AppSupportURL}
AppUpdatesURL={#AppSupportURL}/releases
; Per-user install, no UAC prompt. Existing installations keep their previous folder
; (UsePreviousAppDir is on by default).
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
AllowNoIcons=yes
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
OutputDir=Output
OutputBaseFilename={#AppName}_v{#AppVersion}{#EditionSuffix}_Setup
SetupIconFile=..\ui\resources\icon.ico
SetupLogging=yes
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
; Electron build is x64 only, and requires Windows 10+
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
MinVersion=10.0
; Let the Restart Manager close a running BackupManager before files are replaced
CloseApplications=yes
RestartApplications=no
UninstallDisplayIcon={app}\{#AppExeName}
UninstallDisplayName={#AppName}{#EditionLabel}

[Languages]
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[CustomMessages]
italian.AutoStart=Avvia BackupManager all'accesso a Windows (necessario per i backup automatici)
english.AutoStart=Start BackupManager when I sign in to Windows (required for automatic backups)
italian.StartupGroup=Avvio:
english.StartupGroup=Startup:

[Tasks]
Name: "autostart"; Description: "{cm:AutoStart}"; GroupDescription: "{cm:StartupGroup}"
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[InstallDelete]
; Leftovers of the old Swing/launch4j layout (it shipped jre\ and src\ next to the exe).
; The Electron app keeps them under resources\ instead. User data lives in {#DataDir}, not here.
Type: filesandordirs; Name: "{app}\jre"
Type: filesandordirs; Name: "{app}\src"
; config.enc held the (weakly encrypted) SMTP password of the removed email feature: don't leave it behind
Type: files; Name: "{app}\config.enc"
Type: files; Name: "{app}\README.md"

[Files]
; The whole packaged Electron app (electron-builder "dir" target)
Source: "{#SourceDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExeName}"; AppUserModelID: "{#AppUserModelID}"
Name: "{group}\{cm:UninstallProgram,{#AppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon

[Registry]
; Per-user autostart: the app starts hidden in the tray (see "--background" in ui/src/main/index.ts)
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
  ValueType: string; ValueName: "{#AppName}"; \
  ValueData: """{app}\{#AppExeName}"" --background"; \
  Tasks: autostart; Flags: uninsdeletevalue

[Run]
Filename: "{app}\{#AppExeName}"; \
  Description: "{cm:LaunchProgram,{#StringChange(AppName, '&', '&&')}}"; \
  Flags: nowait postinstall skipifsilent

[UninstallRun]
; Close the app before uninstalling. "/T" also ends the Java backend it spawned (a plain
; java.exe, which can't be safely targeted by name without hitting unrelated Java processes).
Filename: "taskkill.exe"; Parameters: "/f /t /im {#AppExeName}"; \
  Flags: runhidden waituntilterminated; RunOnceId: "KillApp"

[Code]
function DatabasePath(): String;
begin
  Result := ExpandConstant('{#DataDir}\BackupManager.db');
end;

function MarkerPath(): String;
begin
  Result := ExpandConstant('{#DataDir}\{#DemoMarker}');
end;

// The backend applies the demo SQL only while creating a brand-new database, then deletes the
// marker. So: the demo edition writes it only when no database exists yet (an existing user
// keeps their data and settings), and the standard edition removes any stale marker so a
// later fresh database isn't silently created as a demo.
procedure ApplyEditionMarker();
begin
#ifdef DemoEdition
  if not FileExists(DatabasePath()) then
  begin
    ForceDirectories(ExpandConstant('{#DataDir}'));
    SaveStringToFile(MarkerPath(), 'demo', False);
  end;
#else
  if FileExists(MarkerPath()) then
    DeleteFile(MarkerPath());
#endif
end;

// The Restart Manager doesn't know about the Java child process, which keeps the backend JAR
// open: end the whole process tree before files are replaced.
function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ResultCode: Integer;
begin
  Exec('taskkill.exe', '/f /t /im {#AppExeName}', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  Result := '';
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
    ApplyEditionMarker();
end;
