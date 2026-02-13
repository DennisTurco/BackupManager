package test.repositories;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.TestDatabaseInitializer;

public class BackupConfigurationRepositoryTest {
    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }
}
