package test.repositories;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Enums.BackupTriggeredEnum;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.database.TestDatabaseInitializer;

public class BackupRequestRepositoryTest {
    private List<ConfigurationBackup> backups;
    private List<BackupRequest> requests;

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();

        setupBackupRequestList();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

    @Test
    protected void isAnyBackupRunning_shouldBeTrue_forRunningBackup() {
        assertTrue(BackupRequestRepository.isAnyBackupRunning());
    }

    @Test
    protected void updateRequestProgressByRequestId_shouldBeTrue_forDifferentProgressValueAfterUpdate() {
        BackupRequestRepository.updateRequestProgressByRequestId(requests.get(2).backupRequestId(), 50);
        BackupRequest runningRequest = BackupRequestRepository.getBackupRequestById(requests.get(2).backupRequestId());
        assertTrue(requests.get(2).progress() != runningRequest.progress());
    }

    @Test
    protected void updateRequestFolderSizeZippedByRequestId_shouldBeTrue_forDifferentFolderSizeAfterUpdate() {
        BackupRequestRepository.updateRequestFolderSizeZippedByRequestId(requests.get(2).backupRequestId(), 6773);
        BackupRequest runningRequest = BackupRequestRepository.getBackupRequestById(requests.get(2).backupRequestId());
        assertTrue(!Objects.equals(requests.get(2).zippedTargetSize(), runningRequest.zippedTargetSize()));
    }

    @Test
    protected void updateRequestStatusByRequestId_shouldBeTrue_forDifferentStatusAfterUpdate() {
        BackupRequestRepository.updateRequestStatusByRequestId(requests.get(2).backupRequestId(), BackupStatusEnum.FINISHED);
        BackupRequest runningRequest = BackupRequestRepository.getBackupRequestById(requests.get(2).backupRequestId());
        assertTrue(requests.get(2).status() != runningRequest.status());
    }

    @Test
    protected void getLastBackupInProgressByConfigurationId_shouldBeTrue_forExistingBackupRequest() {
        BackupRequest request_in_progress = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backups.get(1).getId());
        assertTrue(request_in_progress != null);
    }

    private void setupBackupRequestList() {
        createBackupConfigurations();

        BackupRequest request1 = createFinishedRequest(backups.get(0).getId());
        BackupRequest request2 = createFinishedRequest(backups.get(1).getId());
        BackupRequest request3 = BackupRequest.createNewBackupRequest(backups.get(1).getId(), BackupTriggeredEnum.USER, "outputPath", 123412, 2);
        BackupRequest request4 = createFinishedRequest(backups.get(1).getId());

        requests = new ArrayList<>();
        requests.add(request1);
        requests.add(request2);
        requests.add(request3);
        requests.add(request4);

        for (BackupRequest request : requests) {
            BackupRequestRepository.insertBackupRequest(request);
        }

        requests = BackupRequestRepository.getRequestBackups();
    }

    private void createBackupConfigurations() {
        ConfigurationBackup backup1 = createConfiguration("Test1");
        ConfigurationBackup backup2 = createConfiguration("Test2");
        ConfigurationBackup backup3 = createConfiguration("Test3");

        backups = new ArrayList<>();
        backups.add(backup1);
        backups.add(backup2);
        backups.add(backup3);

        for (ConfigurationBackup backup : backups) {
            BackupConfigurationRepository.insertBackup(backup);
        }

        backups = BackupConfigurationRepository.getBackupList();
    }

    private ConfigurationBackup createConfiguration(String name) {
        return new ConfigurationBackup(0, name, "TargetPath", "DestinationPath", null, false, null, null, "", LocalDateTime.now(), LocalDateTime.now(), 0, 1);
    }

    private BackupRequest createFinishedRequest(int configurationId) {
        return new BackupRequest(0, configurationId, LocalDateTime.now(), LocalDateTime.now(), BackupStatusEnum.FINISHED, 100, BackupTriggeredEnum.SCHEDULER, Long.valueOf(10), "OutputPath", 1000, Long.valueOf(100), 2, "");
    }
}
