package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;

public class ZippingContext {
    public Backup backup;
    public TrayIcon trayIcon;
    public BackupTable backupTable;
    public BackupProgressGUI progressBar;
    public JButton singleBackupBtn;
    public JToggleButton autoBackupBtn;
    public JMenuItem interruptBackupPopupItem;
    public JMenuItem deleteBackupPopupItem;

    public ZippingContext(Backup backup, TrayIcon trayIcon, BackupTable backupTable, BackupProgressGUI progressBar,
                          JButton singleBackupBtn, JToggleButton autoBackupBtn,
                          JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopupItem) {
        this.backup = backup;
        this.trayIcon = trayIcon;
        this.backupTable = backupTable;
        this.progressBar = progressBar;
        this.singleBackupBtn = singleBackupBtn;
        this.autoBackupBtn = autoBackupBtn;
        this.interruptBackupPopupItem = interruptBackupPopupItem;
        this.deleteBackupPopupItem = deleteBackupPopupItem;
    }
}
