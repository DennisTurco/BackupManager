package backupmanager.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.BackupStatus;
import backupmanager.Helpers.SqlHelper;
import backupmanager.Utils.FolderUtils;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class RunningBackupService {
    public static Optional<BackupRequest> getRunningBackupByName(String backupName) {
        ConfigurationBackup config = BackupConfigurationRepository.getBackupByName(backupName);
        if (config == null) return Optional.empty();

        List<BackupRequest> running = BackupRequestRepository.getRunningBackups();
        return running.stream()
                .filter(r -> r.backupConfigurationId() == config.getId()
                          && r.status() == BackupStatus.IN_PROGRESS)
                .findFirst();
    }

    public static void updateBackupZippedFolderSizeById(int requestId, String pathFolderSize) {
        long folderSize = FolderUtils.calculateFileOrFolderSize(pathFolderSize);
        BackupRequestRepository.updateRequestFolderSizeZippedByRequestId(requestId, folderSize);
    }

    public static void updateBackupStatusAfterForceTerminationByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);
        BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatus.TERMINATED);
    }

    public static void updateBackupStatusAfterCompletitionByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);

        LocalDateTime completionDate = LocalDateTime.now();
        long duration = SqlHelper.toMilliseconds(completionDate) - SqlHelper.toMilliseconds(request.startedDate());

        BackupRequest newRequest = new BackupRequest(request.backupRequestId(), request.backupConfigurationId(), request.startedDate(), completionDate, BackupStatus.FINISHED, 100, request.triggeredBy(), duration, request.outputPath(), request.unzippedTargetSize(), request.zippedTargetSize(), request.filesCount(), request.errorMessage());

        BackupRequestRepository.updateBackupRequestByRequestId(request.backupRequestId(), newRequest);
    }
}
