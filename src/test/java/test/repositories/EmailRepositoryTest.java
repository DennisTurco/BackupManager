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

import backupmanager.Entities.Email;
import backupmanager.Enums.EmailType;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.EmailRepository;
import backupmanager.database.TestDatabaseInitializer;

public class EmailRepositoryTest {

    private List<Email> emails;

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();
        createEmails();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

    @Test
    protected void getLastEmailByType_shouldBeEquals_forLastWelcomeEmailRegistered() {
        Email lastSent = EmailRepository.getLastEmailByType(EmailType.WELCOME);
        assertEquals(EmailType.WELCOME, lastSent.type());
    }

    @Test
    protected void getLastErrorEmailByPayloadAndVersion_shouldBeTrue_forLastErrorEmailRegisteredByData() {
        Email lastSent = EmailRepository.getLastErrorEmailByPayloadAndVersion("thread1 error", "2.1.0");
        assertTrue(lastSent != null);
    }

    private void createEmails() {
        Email email1 = new Email(0, EmailType.WELCOME, LocalDateTime.now(), "2.0.1", null);
        Email email2 = new Email(0, EmailType.CRITICAL_ERROR, LocalDateTime.now().plusHours(6), "2.1.0", "thread1 error");
        Email email3 = new Email(0, EmailType.CRITICAL_ERROR, LocalDateTime.now().plusDays(9), "2.2.1", "a strange error");

        emails = new ArrayList<>();
        emails.add(email1);
        emails.add(email2);
        emails.add(email3);

        for (Email email : emails) {
            EmailRepository.insertEmail(email);
        }
    }
}
