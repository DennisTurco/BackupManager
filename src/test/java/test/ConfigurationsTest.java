package test;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Confingurations;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;
import backupmanager.database.Database;
import backupmanager.database.DatabaseInitializer;
import backupmanager.database.DatabasePaths;

public class ConfigurationsTest {

    private LanguagesEnum language;
    private ThemesEnum theme;

    @BeforeEach
    protected void initDbAndBuildConfigurations() throws Exception {
        Database.init(DatabasePaths.getDatabasePath());
        DatabaseInitializer.init();

        Confingurations.loadAllConfigurations();
        language = Confingurations.getLanguage();
        theme = Confingurations.getTheme();

        buildAndReloadConfigurations();
    }

    @AfterEach
    protected void resetValuesBeforeTest() {
        Confingurations.setLanguage(language);
        Confingurations.setTheme(theme.getThemeName());
        Confingurations.updateAllConfigurations();
    }

    @Test
    protected void equals_shouldReturnTrue_forSameLanguage() throws IOException {
        assertEquals(LanguagesEnum.DEU, Confingurations.getLanguage());
    }

    @Test
    protected void equals_shouldReturnTrue_forSameTheme() throws IOException {
        assertEquals(ThemesEnum.CARBON, Confingurations.getTheme());
    }

    private void buildAndReloadConfigurations() throws IOException {
        buildValidConfigurationsObject();
        realodConfigurations();
    }

    private void buildValidConfigurationsObject()  {
        Confingurations.setLanguage(LanguagesEnum.DEU);
        Confingurations.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void realodConfigurations() {
        Confingurations.updateAllConfigurations();
        Confingurations.loadAllConfigurations();
    }
}