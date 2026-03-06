package test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Configurations;
import backupmanager.Entities.Subscription;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.SubscriptionCreationType;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.SubscriptionRepository;
import backupmanager.database.TestDatabaseInitializer;

class SubscriptionHelperTest {

    private static final String CONFIG = "src/main/resources/res/config/config.json";

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();
        ConfigKey.loadFromJson(CONFIG);
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

     @Test
    protected void getSubscriptionStatus_shouldBeEquals_forExpiredSubscripion() throws SQLException {
        createSubscriptionByStatus(SubscriptionStatus.EXPIRED);
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        assertEquals(SubscriptionStatus.EXPIRED, status);
    }

    @Test
    protected void getSubscriptionStatus_shouldBeEquals_forActiveSubscripion() throws SQLException {
        createSubscriptionByStatus(SubscriptionStatus.ACTIVE);
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        assertEquals(SubscriptionStatus.ACTIVE, status);
    }

    @Test
    protected void getSubscriptionStatus_shouldBeEquals_forExpiringSubscripion() throws SQLException {
        createSubscriptionByStatus(SubscriptionStatus.EXPIRATION);
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        assertEquals(SubscriptionStatus.EXPIRATION, status);
    }

    @Test
    protected void getSubscriptionStatus_shouldBeEquals_forNoneSubscripion() throws SQLException {
        createSubscriptionByStatus(SubscriptionStatus.NONE);
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        assertEquals(SubscriptionStatus.NONE, status);
    }

    @Test
    protected void getSubscriptionStatusTranslated_shouldBeTrue_forSubscriptionValidStatus() {
        String translationActive = SubscriptionHelper.getSubscriptionStatusTranslated(SubscriptionStatus.ACTIVE);
        String translationExpiration= SubscriptionHelper.getSubscriptionStatusTranslated(SubscriptionStatus.EXPIRATION);
        String translationExpired = SubscriptionHelper.getSubscriptionStatusTranslated(SubscriptionStatus.EXPIRED);
        assertTrue(!translationActive.isBlank() && !translationExpiration.isEmpty() && !translationExpired.isEmpty());
    }

    @Test
    protected void getSubscriptionStatusTranslated_shouldBeTrue_forNoNeddedSubscriptionStatus() {
        String translation = SubscriptionHelper.getSubscriptionStatusTranslated(SubscriptionStatus.NONE);
        assertTrue(translation.isEmpty());
    }

    private void createSubscriptionByStatus(SubscriptionStatus status) throws SQLException {
        LocalDate start = LocalDate.now().plusDays(-1);
        LocalDate end = LocalDate.now();
        Configurations.setSubscriptionNedded(true);
        switch (status) {
            case ACTIVE -> end = end.plusDays(30);
            case EXPIRATION -> end = end.plusDays(5);
            case EXPIRED -> end = end.plusDays(-1);
            case NONE -> Configurations.setSubscriptionNedded(false);
        }

        Subscription sub = new Subscription(0, LocalDateTime.now(), start, end, SubscriptionCreationType.MANUAL);
        SubscriptionRepository.deleteSubscriptions();
        SubscriptionRepository.insertSubscription(sub);
    }
}
