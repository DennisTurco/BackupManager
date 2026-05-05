package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;

import backupmanager.gui.Table.BackupTableDataService;
import backupmanager.gui.frames.BackupProgressGUI;

public record BackupUIContext (
    TrayIcon trayIcon,
    BackupTableDataService backupTableService,
    BackupProgressGUI progressBar,
    JMenuItem interruptBackupPopupItem,
    JMenuItem deleteBackupPopupItem
) { }
