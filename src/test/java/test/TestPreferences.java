package test;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;

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
        Preferences.setLanguage(LanguagesEnum.ENG);
        Preferences.setTheme(ThemesEnum.INTELLIJ);

        Preferences.updatePreferencesToJSON(); // update
        Preferences.loadPreferencesFromJSON(); // reload

        // check if update changed everything correctly
        assertEquals(LanguagesEnum.ENG.getLanguageName(), Preferences.getLanguage().getLanguageName());
        assertEquals(ThemesEnum.INTELLIJ.getThemeName(), Preferences.getTheme().getThemeName());
    }

    @AfterEach
    void tearDown() {
        if (temp_log_file != null && temp_log_file.exists()) {
            temp_log_file.delete();
        }
    }
}