package backupmanager.Services;

import java.time.LocalDateTime;

import backupmanager.Entities.BackupRequest;
import backupmanager.Enums.BackupStatus;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.Utils.FolderUtils;

public class RunningBackupService {

    public static void updateBackupZippedFolderSizeById(int requestId, String pathFolderSize) {
        long folderSize = FolderUtils.calculateFileOrFolderSize(pathFolderSize);
        BackupRequestRepository.updateRequestFolderSizeZippedByRequestId(requestId, folderSize);
    }

    // Called mid-flight from ZipFileVisitor while the output ZipOutputStream is still open —
    // only marks the DB status here. ZippingThread deletes the partial file once the stream
    // is safely closed (deleting an open file handle silently fails on Windows).
    public static void updateBackupStatusAfterForceTerminationByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);
        if (request == null) {
            return;
        }
        BackupHelper.markTerminated(request);
    }

    public static void updateBackupStatusAfterCompletitionByBackupConfigurationId(int backupConfigurationId) {
        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupConfigurationId);

        if (request == null) {
            return;
        }

        LocalDateTime completionDate = LocalDateTime.now();
        long duration = SqlHelper.toMilliseconds(completionDate) - SqlHelper.toMilliseconds(request.startedDate());

        BackupRequest newRequest = new BackupRequest(request.backupRequestId(), request.backupConfigurationId(), request.startedDate(), completionDate, BackupStatus.FINISHED, 100, request.triggeredBy(), duration, request.outputPath(), request.unzippedTargetSize(), request.zippedTargetSize(), request.filesCount(), request.errorMessage());

        BackupRequestRepository.updateBackupRequestByRequestId(request.backupRequestId(), newRequest);
    }
}
