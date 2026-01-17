package test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;

public class PreferencesTest {

    private final String directory = "src/test/resources/";
    private final String filename = "tempLogFile.json";
    private File tempLogFile;

    @Test
    public void equals_shouldReturnTrue_forSameLanguage() throws IOException {
        buildAndReloadPreferences();
        assertEquals(LanguagesEnum.DEU, Preferences.getLanguage());
    }

    @Test
    public void equals_shouldReturnTrue_forSameTheme() throws IOException {
        buildAndReloadPreferences();
        assertEquals(ThemesEnum.CARBON, Preferences.getTheme());
    }

    private void buildAndReloadPreferences() throws IOException {
        buildTempFile();
        buildValidPreferencesObject();
        realodPreferences();

        deleteTempFile();
    }

    private void buildTempFile() throws IOException {
        tempLogFile = File.createTempFile(directory + filename, "");
    }

    private void buildValidPreferencesObject()  {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void realodPreferences() {
        Preferences.updateAllPreferences();
        Preferences.loadAllPreferences();
    }

    private void deleteTempFile() {
        if (tempLogFile.exists()) {
            tempLogFile.delete();
        }
    }

}