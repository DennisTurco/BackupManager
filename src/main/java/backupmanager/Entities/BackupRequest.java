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
    String outputPath,
    long unzippedTargetSize,
    Long zippedTargetSize,
    int filesCount,
    String errorMessage
)
{
    public static BackupRequest createNewBackupRequest(int backupConfigurationId, BackupTriggeredEnum type, String outputPath, long targetSize, int filesCount) {
        return new BackupRequest(0, backupConfigurationId, LocalDateTime.now(), null, BackupStatusEnum.IN_PROGRESS, 0, type, null, outputPath, targetSize, null, filesCount, null);
    }
}
