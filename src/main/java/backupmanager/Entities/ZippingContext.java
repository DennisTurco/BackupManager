package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;

import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;
import backupmanager.Utils.FolderUtils;

public record ZippingContext (
    ConfigurationBackup backup,
    TrayIcon trayIcon,
    BackupTable backupTable,
    BackupProgressGUI progressBar,
    JMenuItem interruptBackupPopupItem,
    JMenuItem deleteBackupPopupItem,
    long folderUnzippedSize
) {
    public static ZippingContext create(ConfigurationBackup backup, TrayIcon trayIcon, BackupTable backupTable, BackupProgressGUI progressBar, JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopupItem) {
        long folderSize = FolderUtils.calculateFileOrFolderSize(backup.getTargetPath());
        return new ZippingContext(backup, trayIcon, backupTable, progressBar, interruptBackupPopupItem, deleteBackupPopupItem, folderSize);
    }
}
