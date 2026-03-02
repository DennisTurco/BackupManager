package test;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.User;
import backupmanager.Services.LoginService;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.TestDatabaseInitializer;

public class LoginServiceTest {
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
    protected void isFirstAccess_shouldBeTrue_whenNoUserExists() {
        LoginService loginService = new LoginService();
        boolean isFirstAccess = loginService.isFirstAccess();
        assertTrue(isFirstAccess);
    }

    @Test
    protected void createNewUser_shouldBeAbleToCreateUser() {
        LoginService loginService = new LoginService();
        loginService.createNewUser(new User("TestName", "TestSurname", "test@gmail.com"));
        boolean isFirstAccess = loginService.isFirstAccess();
        assertTrue(!isFirstAccess);
    }
}
