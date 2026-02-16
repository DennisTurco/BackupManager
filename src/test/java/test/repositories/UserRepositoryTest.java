package test.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.User;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.Repositories.UserRepository;
import backupmanager.database.TestDatabaseInitializer;

public class UserRepositoryTest {
    private List<User> users;

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();

        setupUserList();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

    @Test
    protected void getLastUser_shouldBeEquals_forSameUser() {
        User lastUser = UserRepository.getLastUser();
        assertEquals(users.getLast().email(), lastUser.email());
    }

    private void setupUserList() {
        User user1 = new User("Username1", "Usersurname1", "username1@gmail.com");
        User user2 = new User("Username2", "Usersurname2", "username2@gmail.com");
        User user3 = new User("Username3", "Usersurname3", "username3@gmail.com");

        users = new ArrayList<>();

        users.add(user1);
        users.add(user2);
        users.add(user3);

        for (User user : users) {
            UserRepository.insertUser(user);
        }
    }
}
