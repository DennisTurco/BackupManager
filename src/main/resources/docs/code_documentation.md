<center> <img src="../res/img/logo.png" height="150"> </center>

# Backup Manager Documentation

## Startup backgroud service Logic
```mermaid
graph TD
  n1(((PC Startup))) --> n2(Start Background Service)
  n2 --> n3(Start TrayIcon)
  n2 --> n4(Periodic Auto Backup Check)
  n3 -->|click| n5(Start GUI)
  n3 -->|exit| n6(Shutdown Backup Service)
```

## Link
* [SVG](https://www.svgrepo.com/)

## Dependecies
For this project i'm using some dependencies:
* **Flatlaf** for multi theme ([demo](https://www.formdev.com/flatlaf/#demo), [themes](https://www.formdev.com/flatlaf/themes/), [github](https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-intellij-themes)) 
* **Gson** for manage json data
* **itextpdf** for pdf export
* **flatlaf-extras** to use svg images ([website]( https://mvnrepository.com/artifact/com.formdev/flatlaf-extras))

## Threads
### [BackgroundService](../../java/backupmanager/Services/BackupService.java)
This thread is executed only on PC startup. <br>
It is used for

### [RunningBackupObserver](../../java/backupmanager/Services/RunningBackupObserver.java)
I need a thread that constantly checks if there are something running and i can't use a simple method calls instead because
if a backup starts caused by the BackugroundService and we open the GUI, thre are 2 different instance of this program, 
so we need something like an observer that constantly checks if there are some backups in progress.