package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;

import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;

public record ZippingContext (
    Backup backup,
    TrayIcon trayIcon,
    BackupTable backupTable,
    BackupProgressGUI progressBar,
    JMenuItem interruptBackupPopupItem,
    JMenuItem deleteBackupPopupItem
) { }
