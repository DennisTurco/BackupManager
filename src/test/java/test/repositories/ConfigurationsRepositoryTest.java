package test.repositories;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Confingurations;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.TestDatabaseInitializer;

public class ConfigurationsRepositoryTest {

    @BeforeEach
    protected void setup() throws Exception {
        Database.init(DatabasePaths.getTestDatabasePath());
        TestDatabaseInitializer.init();

        Confingurations.loadAllConfigurations();

        buildAndReloadConfigurations();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
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

    private void buildValidConfigurationsObject() {
        Confingurations.setLanguage(LanguagesEnum.DEU);
        Confingurations.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void realodConfigurations() {
        Confingurations.updateAllConfigurations();
        Confingurations.loadAllConfigurations();
    }
}
