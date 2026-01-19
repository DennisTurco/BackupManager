package test;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;
import backupmanager.Repositories.Repository;

public class PreferencesTest {

    private LanguagesEnum language;
    private ThemesEnum theme;

    @BeforeEach
    protected void initDbAndBuildPreferences() throws Exception {
        Repository.initDatabaseIfNotExists();

        Preferences.loadAllPreferences();
        language = Preferences.getLanguage();
        theme = Preferences.getTheme();

        buildAndReloadPreferences();
    }

    @AfterEach
    protected void resetValuesBeforeTest() {
        Preferences.setLanguage(language);
        Preferences.setTheme(theme.getThemeName());
        Preferences.updateAllPreferences();
    }

    @Test
    protected void equals_shouldReturnTrue_forSameLanguage() throws IOException {
        assertEquals(LanguagesEnum.DEU, Preferences.getLanguage());
    }

    @Test
    protected void equals_shouldReturnTrue_forSameTheme() throws IOException {
        assertEquals(ThemesEnum.CARBON, Preferences.getTheme());
    }

    private void buildAndReloadPreferences() throws IOException {
        buildValidPreferencesObject();
        realodPreferences();
    }

    private void buildValidPreferencesObject()  {
        Preferences.setLanguage(LanguagesEnum.DEU);
        Preferences.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void realodPreferences() {
        Preferences.updateAllPreferences();
        Preferences.loadAllPreferences();
    }
}