![logo](./src/main/resources/res/img/banner.png)

# Backup Manager

**Backup Manager** is a user-friendly program with an intuitive graphical interface, designed to simplify and automate the backup of folders and subfolders. Users can configure a custom time interval between automatic backups, setting the desired number of days between each operation. Additionally, manual backups can be performed at any time, providing maximum flexibility.

Each backup is carefully saved, and the program maintains a detailed log of all completed operations. Users can also view, manage, and edit the details of each backup, ensuring complete control and customization over saved data. This tool is an ideal solution for efficiently and securely protecting files, minimizing the risk of data loss.

## Features

* 📁 Automatic backup of folders and subfolders
* 🕒 Flexible scheduling for recurring backups
* 🖱️ Simple GUI with tray icon support
* 📝 Detailed logs and backup history
* 🎨 Light/Dark themes and multilingual support (EN, IT, DE, ES, FR)
* 🪟 Auto-start with the operating system (configurable)

## Screenshots and Videos

![gif](./docs/imgs/BackupManagerPresentation.gif)

| ![image1](./docs/imgs/AutoBackup.png) | ![image2](./docs/imgs/BackupList.png) |
| ------------------------ | ------------------------ |
| ![image3](./docs/imgs/CompletedBackup.png) | ![image4](./docs/imgs/ThemeLanguage.png) |

### Multi theme

| ![image5](./docs/imgs/Home.png) | ![image6](./docs/imgs/Home2.png) |
| ------------------------ | ------------------------ |
| ![image7](./docs/imgs/Home3.png) | . |

## Architecture

```
BackupManager/
├── src/main/java/backupmanager/   # Java backend (Maven)
│   ├── api/                       # REST API layer (Javalin 6 on port 7070)
│   │   └── routes/                # BackupRoutes, AnalyticsRoutes, SettingsRoutes, LogRoutes, AuthRoutes
│   ├── Services/                  # BackgroundService (scheduler), backup engine
│   ├── Repositories/              # SQLite persistence via JDBC
│   └── MainApp.java               # Entry point — GUI | background | api-server mode
└── ui/                            # Electron + React + TypeScript frontend
    ├── src/main/index.ts          # Electron main — spawns Java JAR with --api-server flag
    ├── src/preload/index.ts       # Exposes env.apiBase to the renderer
    └── src/renderer/src/          # React app (Vite)
        ├── pages/                 # BackupTablePage, DashboardPage, HistoryPage, SettingsPage
        ├── services/api.ts        # Axios client for all REST endpoints
        └── context/ThemeContext   # Dark / light theme
```

**Runtime flow:** Electron spawns `java -jar BackupManager.jar --api-server` → Java starts Javalin REST server on `http://localhost:7070` → React renderer calls the API via Axios.

## Local Development Setup

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 21+     |
| Maven | 3.9+   |
| Node.js | 20+  |
| npm  | 10+     |

### 1 — Build the Java backend

```bash
mvn clean package -DskipTests
```

This produces `target/BackupManager-<version>.jar`.

### 2 — Run the REST API server standalone (optional, for UI-only dev)

```bash
java -jar target/BackupManager-*.jar --api-server
```

The API will be available at `http://localhost:7070/api/status`. Keep this terminal open.

### 3 — Start the React + Electron UI

```bash
cd ui
npm install
npm run dev
```

This opens Electron in development mode with hot-reload. Electron automatically spawns the Java JAR; if you are already running the JAR manually (step 2), comment out the `spawnJar` call in `src/main/index.ts` to avoid a port conflict.

### 4 — Build a distributable package

```bash
# Build Java JAR first
mvn clean package -DskipTests

# Then package Electron app (bundles the JAR inside)
cd ui
npm run build
npm run dist   # or: npm run package
```

### Available npm scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start Electron + Vite dev server with HMR |
| `npm run build` | Compile TypeScript + Vite bundle |
| `npm run preview` | Preview the built renderer in a browser |
| `npm run dist` | Build distributable (via electron-builder) |

### Code quality — Java

```bash
mvn clean verify
```

Runs Checkstyle, SpotBugs and unit tests. The build fails on any violation.

## Code Documentation

$\rightarrow$ [Code technical documentation](./code_documentation.md)

## Important Notes

* If, for any reason, the setup program doesn't add the application to the startup registry (`regedit`), you can manually run "add_to_startup.bat" located in the installation folder by double-clicking it.
* This program is set to run automatically at PC startup by default. If you disable it, automatic backups will no longer occur.

## Platforms

| Platform | Availability |
| --- | --- |
| Windows | ✅ |
| Linux | ✅ (via Electron) |
| MacOS | ✅ (via Electron) |

## Supported Languages

| Piattaforma | Availability |
| --- | --- |
| English | ✅ |
| Italian | ✅ |
| Spanish | ✅ |
| German | ✅ |
| French | ✅ |

## Code Quality

This project enforces automatic code quality checks during the Maven verify phase.
Running the following command will execute formatting checks, static analysis, and tests:

`mvn clean verify`

If any rule is violated, the build will fail.

## Licence

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

## Time report

[![wakatime](https://wakatime.com/badge/user/ce36d0fc-2f0b-4e85-b318-872804ab18b6/project/882e0afb-87a6-495d-9082-a9de9f9f4f19.svg)](https://wakatime.com/badge/user/ce36d0fc-2f0b-4e85-b318-872804ab18b6/project/882e0afb-87a6-495d-9082-a9de9f9f4f19)

## Authors

* [DennisTurco](https://www.github.com/DennisTurco)

## Support

For support, email: [dennisturco@gmail.com](dennisturco@gmail.com)
