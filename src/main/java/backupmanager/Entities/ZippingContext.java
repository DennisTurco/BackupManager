package backupmanager.Entities;

import java.awt.TrayIcon;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import backupmanager.gui.frames.BackupProgressGUI;
import backupmanager.utils.FolderUtils;

public record ZippingContext (
    ConfigurationBackup backup,
    TrayIcon trayIcon,
    JTable backupTable,
    BackupProgressGUI progressBar,
    JMenuItem interruptBackupPopupItem,
    JMenuItem deleteBackupPopupItem,
    long folderUnzippedSize
) {
    public static ZippingContext create(ConfigurationBackup backup, TrayIcon trayIcon, JTable backupTable, BackupProgressGUI progressBar, JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopupItem) {
        long folderSize = FolderUtils.calculateFileOrFolderSize(backup.getTargetPath());
        return new ZippingContext(backup, trayIcon, backupTable, progressBar, interruptBackupPopupItem, deleteBackupPopupItem, folderSize);
    }
}
