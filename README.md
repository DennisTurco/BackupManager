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

## Code Documentation

$\rightarrow$ [Code technical documentation](./code_documentation.md)

## Important Notes

* If, for any reason, the setup program doesn't add the application to the startup registry (`regedit`), you can manually run "add_to_startup.bat" located in the installation folder by double-clicking it.
* This program is set to run automatically at PC startup by default. If you disable it, automatic backups will no longer occur.

## Platforms

| Platform | Availability |
| --- | --- |
| Windows | ✅ |
| Linux | ❌ |
| MacOS | ❌ |

## Supported Languages

| Piattaforma | Availability |
| --- | --- |
| English | ✅ |
| Italian | ✅ |
| Spanish | ✅ |
| German | ✅ |
| French | ✅ |

## Code Quality

This project uses automated code quality analysis on every Pull Request targeting `master`.
The analysis runs the following tools and posts a summary comment directly on the PR:

| Tool | What it checks |
|------|---------------|
| JaCoCo | Test coverage (line & branch) |
| SpotBugs + find-sec-bugs | Bugs and security vulnerabilities |
| PMD | Code complexity and style violations |
| CPD | Code duplication (blocks > 100 tokens) |

To run the analysis locally:

```powershell
./analyze.ps1
```

Or run individual checks:

```powershell
./mvnw clean verify "-Dmaven.test.failure.ignore=true"   # tests + coverage
./mvnw spotbugs:spotbugs                                  # bug analysis
./mvnw pmd:pmd pmd:cpd                                    # complexity + duplication
```

## Licence

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

## Time report

[![wakatime](https://wakatime.com/badge/user/ce36d0fc-2f0b-4e85-b318-872804ab18b6/project/882e0afb-87a6-495d-9082-a9de9f9f4f19.svg)](https://wakatime.com/badge/user/ce36d0fc-2f0b-4e85-b318-872804ab18b6/project/882e0afb-87a6-495d-9082-a9de9f9f4f19)

## Authors

* [DennisTurco](https://www.github.com/DennisTurco)

## Support

For support, email: [dennisturco@gmail.com](dennisturco@gmail.com)
