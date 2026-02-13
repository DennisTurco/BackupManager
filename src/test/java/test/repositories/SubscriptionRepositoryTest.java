package test.repositories;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Subscription;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.SubscriptionRepository;
import backupmanager.database.TestDatabaseInitializer;

public class SubscriptionRepositoryTest {
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
    protected void getAnySubscriptionValid_shouldBeTrue_forDemoSubscription() {
        Subscription demoSub = SubscriptionRepository.getAnySubscriptionValid();
        assertTrue(demoSub != null);
    }
}
