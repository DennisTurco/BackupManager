# Building the Windows installer

BackupManager ships as an [Inno Setup](https://jrsoftware.org/isinfo.php) installer that wraps the
Electron app packaged by electron-builder. There are two editions, built from the same sources:

| Edition  | Script                                        | Output                                 | Subscription |
|----------|-----------------------------------------------|----------------------------------------|--------------|
| Standard | `installer/BackupManager_installer.iss`       | `BackupManager_v<ver>_Setup.exe`       | Not required: every feature is always active |
| Demo     | `installer/BackupManager_installer_demo.iss`  | `BackupManager_v<ver>_Demo_Setup.exe`  | Enabled, with a 1 month free trial |

Both edition scripts only set a couple of macros and `#include` `installer/BackupManager_common.iss`,
which holds the whole installer definition. Change the common file, not the edition scripts, and never
compile `BackupManager_common.iss` directly (it stops with an error telling you which file to compile).

## Prerequisites

| Tool | Notes |
|------|-------|
| JDK 21 | Used by the Maven wrapper to build the backend |
| Node.js 20+ / npm 10+ | Used to build and package the Electron app |
| [Inno Setup 6.3+](https://jrsoftware.org/isdl.php) | 6.3 or newer is required for `ArchitecturesAllowed=x64compatible` |
| `jre\` folder in the repository root | A Windows x64 Java 21 runtime. It is **not** in git (`/jre/` is ignored): copy a JRE/JDK there before packaging. It is bundled into the app so users don't need Java installed |

## Steps

Run everything from the repository root.

### 1. Build the backend JAR

```bash
./mvnw.cmd clean package -DskipTests
```

Produces `target\backupmanager-jar-with-dependencies.jar`.

### 2. Package the Electron app

```bash
cd ui
npm install
npm run dist:win
```

`dist:win` builds the renderer/main bundles and runs electron-builder with the `dir` target (no NSIS:
Inno Setup is the installer). The result is `ui\release\win-unpacked\`, which contains
`BackupManager.exe` and, under `resources\`:

- `backend.jar` — the backend JAR from step 1
- `jre\` — the bundled Java runtime
- `src\main\resources\` — configuration, languages and database scripts read by the backend
- `icon.ico`

These come from `build.extraResources` in `ui/package.json`.

### 3. Compile the installers

Either open an edition script in the Inno Setup Compiler and press **Ctrl+F9**, or from the command line:

```bash
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\BackupManager_installer.iss
```

```bash
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\BackupManager_installer_demo.iss
```

The installers are written to `installer\Output\` (ignored by git).

To package a build that lives somewhere else, override the source folder and/or the output folder:

```bash
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" /DSourceDir=D:\build\win-unpacked /OD:\build\installers installer\BackupManager_installer.iss
```

## Releasing a new version

The version number lives in three places; keep them in sync:

| File | Field | Used for |
|------|-------|----------|
| `installer/BackupManager_common.iss` | `#define AppVersion` | Installer name, "Apps & features" entry |
| `ui/package.json` | `version` | `BackupManager.exe` file version |
| `src/main/resources/res/config/config.json` | `VERSION` | Version shown in the app |

## What the installer does

- Installs per user (`{autopf}\BackupManager`, usually `%LOCALAPPDATA%\Programs\BackupManager`), with no
  administrator prompt. An existing installation is upgraded in its current folder.
- Supports Italian and English.
- **Start with Windows** (task selected by default): adds an `HKCU\...\Run` entry that launches
  `BackupManager.exe --background`, which starts hidden in the tray. Automatic backups only run while the
  app is running. The entry is removed on uninstall.
- Optional desktop shortcut; Start menu shortcuts are always created.
- Closes a running BackupManager (including its Java backend process) before installing and before
  uninstalling.
- Upgrades from the old Swing installer in place (same `AppId`) and removes its leftovers
  (`jre\`, `src\`, `config.enc`, `README.md` next to the exe).
- Never touches user data: the database and settings live in `Documents\Shard\data`, logs in
  `%USERPROFILE%\.backupmanager\logs`.

### Demo edition

On a machine with **no** BackupManager database yet, the demo installer writes a `.demo-init` marker
in `Documents\Shard\data`. On first launch the backend (`ProductionDatabaseInitializer`) sees it while
creating the new database, applies `db/003_enable_demo_version.sql` (subscription enabled, 1 month trial)
and deletes the marker. If a database already exists nothing changes, so existing users keep their data
and settings.

The standard installer deletes any leftover marker, so a database created later is never turned into a
demo by accident.

## Testing

Test the installer in a virtual machine or on a spare Windows user account: installing on your
development machine registers the app in "Apps & features", creates the autostart entry and uses your
real `Documents\Shard\data` database. Check at least:

1. A fresh install of each edition: the app starts, the tray icon appears, and the Subscription page
   shows "not required" (standard) or the trial (demo).
2. An upgrade over the previous version keeps configurations and history.
3. Uninstall removes the app, the shortcuts and the autostart entry, and leaves `Documents\Shard\data`.

## Troubleshooting

**`npm run dist:win` fails with `EBUSY` / "resource busy or locked" on an `.asar` file.**
VS Code (itself an Electron app) can keep `.asar` files open after browsing them in the explorer. Close
VS Code and run the command from an external terminal, or package into another folder and point Inno Setup
at it:

```bash
cd ui
npx electron-builder --win dir -c.directories.output=D:\build
```

```bash
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" /DSourceDir=D:\build\win-unpacked installer\BackupManager_installer.iss
```

**The installed app can't start the backend.** Check that `resources\jre\bin\java.exe` and
`resources\backend.jar` exist in the install folder. If `jre\` is missing, it wasn't in the repository
root when you ran `npm run dist:win`.

**The installer is large (~250 MB).** Most of it is the bundled runtime: if `jre\` is a full JDK
(~300 MB), replacing it with a runtime built by `jlink` (only the modules the backend needs) makes the
installer much smaller.
