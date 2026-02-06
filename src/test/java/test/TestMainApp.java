package test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import backupmanager.MainApp;

public class TestMainApp {

    @Test
    void testWrongArgument() {
        String[] arguments = {"--wrong_argument"};
        assertThrows(IllegalArgumentException.class, () -> MainApp.main(arguments), "Argument passed is not valid!");
    }
}
