package backupmanager.Entities;

import java.time.LocalDateTime;

import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Enums.BackupTriggeredEnum;

public record BackupRequest (
    int backupRequestId,
    int backupConfigurationId,
    LocalDateTime startedDate,
    LocalDateTime completionDate,
    BackupStatusEnum status,
    int progress,
    BackupTriggeredEnum triggeredBy,
    Long durationMs,
    long unzippedTagetSize,
    Long zippedTagetSize,
    int filesCount,
    String errorMessage
)
{
    public static BackupRequest createNewBackupRequest(int backupConfigurationId, BackupTriggeredEnum type, int progress, long targetSize) {
        return new BackupRequest(0, backupConfigurationId, LocalDateTime.now(), null, BackupStatusEnum.IN_PROGRESS, progress, type, null, targetSize, null, 0, null);
    }
}
