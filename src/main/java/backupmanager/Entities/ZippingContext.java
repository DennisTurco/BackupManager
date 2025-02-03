package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;

import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;

public class ZippingContext {
    public Backup backup;
    public TrayIcon trayIcon;
    public BackupTable backupTable;
    public BackupProgressGUI progressBar;
    public JMenuItem interruptBackupPopupItem;
    public JMenuItem deleteBackupPopupItem;

    public ZippingContext(Backup backup, TrayIcon trayIcon, BackupTable backupTable, BackupProgressGUI progressBar,
                          JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopupItem) {
        this.backup = backup;
        this.trayIcon = trayIcon;
        this.backupTable = backupTable;
        this.progressBar = progressBar;
        this.interruptBackupPopupItem = interruptBackupPopupItem;
        this.deleteBackupPopupItem = deleteBackupPopupItem;
    }
}
