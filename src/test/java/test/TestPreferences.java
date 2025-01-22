package test;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class TestPreferences {

    private static File temp_log_file;
    private static final String CONFIG = "src/main/resources/res/config/config.json";

    @BeforeAll
    static void setUpBeforeClass() throws IOException {
        ConfigKey.loadFromJson(CONFIG);

        temp_log_file = File.createTempFile("src/test/resources/temp_log_file", "");
    }

    @Test
    void testUpdatePreferences() {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON);

        Preferences.updatePreferencesToJSON(); // update
        Preferences.loadPreferencesFromJSON(); // reload

        // check if update changed everything correctly
        assertEquals(LanguagesEnum.DEU.getLanguageName(), Preferences.getLanguage().getLanguageName());
        assertEquals(ThemesEnum.CARBON.getThemeName(), Preferences.getTheme().getThemeName());
    }

    @AfterEach
    void tearDown() {
        if (temp_log_file != null && temp_log_file.exists()) {
            temp_log_file.delete();
        }
    }
}