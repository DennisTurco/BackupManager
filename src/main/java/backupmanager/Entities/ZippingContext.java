package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;

import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;
import lombok.Getter;

public class ZippingContext {
    @Getter private Backup backup;
    @Getter private TrayIcon trayIcon;
    @Getter private BackupTable backupTable;
    @Getter private BackupProgressGUI progressBar;
    @Getter private JMenuItem interruptBackupPopupItem;
    @Getter private JMenuItem deleteBackupPopupItem;

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
