package backupmanager.Entities;

import java.time.LocalDateTime;

import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.BackupTriggerType;

public record BackupRequest (
    int backupRequestId,
    int backupConfigurationId,
    LocalDateTime startedDate,
    LocalDateTime completionDate,
    BackupStatus status,
    int progress,
    BackupTriggerType triggeredBy,
    Long durationMs,
    String outputPath,
    long unzippedTargetSize,
    Long zippedTargetSize,
    int filesCount,
    String errorMessage
)
{
    public static BackupRequest createNewBackupRequest(int backupConfigurationId, BackupTriggerType type, String outputPath, long targetSize, int filesCount) {
        return new BackupRequest(0, backupConfigurationId, LocalDateTime.now(), null, BackupStatus.IN_PROGRESS, 0, type, null, outputPath, targetSize, null, filesCount, null);
    }
}
