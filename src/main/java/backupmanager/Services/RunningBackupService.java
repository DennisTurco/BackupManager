package backupmanager.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class RunningBackupService {
    public static Optional<BackupRequest> getRunningBackupByName(String backupName) {
        ConfigurationBackup config = BackupConfigurationRepository.getBackupByName(backupName);
        if (config == null) return Optional.empty();

        List<BackupRequest> running = BackupRequestRepository.getRunningBackups();
        return running.stream()
                .filter(r -> r.backupConfigurationId() == config.getId()
                          && r.status() == BackupStatusEnum.IN_PROGRESS)
                .findFirst();
    }

    public static void updateBackupStatusAfterCompletitionByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);

        updateStatusBasedOnProgressValue(request);
    }

    public static void updateBackupStatusAfterCompletitionByBackupConfigurationId(int backupConfigurationId, long folderSizeZipped) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);

        updateStatusBasedOnProgressValue(request);

        LocalDateTime completionDate = LocalDateTime.now();
        long duration = SqlHelper.toMilliseconds(completionDate) - SqlHelper.toMilliseconds(request.startedDate());

        BackupRequest newRequest = new BackupRequest(request.backupRequestId(), request.backupConfigurationId(), request.startedDate(), completionDate, BackupStatusEnum.FINISHED, 100, request.triggeredBy(), duration, request.outputPath(), request.unzippedTargetSize(), folderSizeZipped, request.filesCount(), request.errorMessage());

        BackupRequestRepository.updateBackupRequestByRequestId(request.backupRequestId(), newRequest);
    }

    private static void updateStatusBasedOnProgressValue(BackupRequest request) {
        if (request.progress() == 100)
            BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatusEnum.FINISHED);
        else
            BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatusEnum.TERMINATED);
    }
}
