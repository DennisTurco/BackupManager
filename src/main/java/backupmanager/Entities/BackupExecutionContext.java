package backupmanager.Entities;

import backupmanager.Utils.FolderUtils;

public record  BackupExecutionContext (
    ConfigurationBackup backup,
    long folderUnzippedSize
) {
    public static BackupExecutionContext create(ConfigurationBackup backup) {
        long folderSize = FolderUtils.calculateFileOrFolderSize(backup.getTargetPath());
        return new BackupExecutionContext(backup, folderSize);
    }
}
