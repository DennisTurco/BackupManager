package backupmanager.Services;

import java.util.List;
import java.util.Optional;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.BackupStatusEnum;
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

    public static boolean isBackupRunning(ConfigurationBackup config) {
        List<BackupRequest> running = BackupRequestRepository.getRunningBackups();
        return running.stream()
                .anyMatch(r -> r.backupConfigurationId() == config.getId()
                           && r.status() == BackupStatusEnum.IN_PROGRESS);
    }

    public static boolean isAnyBackupRunning() {
        List<BackupRequest> running = BackupRequestRepository.getRunningBackups();
        return running.stream().anyMatch(r -> r.status() == BackupStatusEnum.IN_PROGRESS);
    }

    public static void updateBackupStatusAfterCompletitionByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getBackupByConfigurationId(backupConfigurationId);

        if (request.progress() == 100)
            BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatusEnum.FINISHED);
        else
            BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatusEnum.TERMINATED);
    }
}
