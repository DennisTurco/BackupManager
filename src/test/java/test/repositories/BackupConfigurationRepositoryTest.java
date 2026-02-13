package test.repositories;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.TestDatabaseInitializer;

public class BackupConfigurationRepositoryTest {

    private List<ConfigurationBackup> backups;

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

    @Test
    protected void insertBackup_shuldBeEquals_fetchFromBackupName() {
        setupTheBackupList();
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupByName(backups.get(1).getName());
        assertEquals(backup.getName(), backups.get(1).getName());
    }

    @Test
    protected void deleteBackup_shuldBeTrue_afterDelete() {
        setupTheBackupList();
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupByName(backups.get(1).getName());
        BackupConfigurationRepository.deleteBackup(backup.getId());
        ConfigurationBackup backupDeleted = BackupConfigurationRepository.getBackupById(backup.getId());
        assertTrue(backupDeleted == null);
    }

    @Test
    protected void getBackupList_shuldBeTrue_SameSizeForSameBackupList() {
        setupTheBackupList();
        List<ConfigurationBackup> backupList = BackupConfigurationRepository.getBackupList();
        assertTrue(backups.size() == backupList.size());
    }

    private void setupTheBackupList() {
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
    }

    private ConfigurationBackup createConfiguration(String name) {
        return new ConfigurationBackup(0, name, "TargetPath", "DestinationPath", null, false, null, null, "", LocalDateTime.now(), LocalDateTime.now(), 0, 1);
    }
}
