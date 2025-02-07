package test;

import backupmanager.MainApp;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMainApp {
    @Test
    void testWrongArgument() {
        String[] arguments = {"--wrong_argument"};
        assertThrows(IllegalArgumentException.class, () -> MainApp.main(arguments), "Argument passed is not valid!");
    }
}
