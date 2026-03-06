package test.repositories;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.Configurations;
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

        Configurations.loadAllConfigurations();

        buildAndReloadConfigurations();
    }

    @AfterEach
    protected void clean() throws IOException {
        TestDatabaseInitializer.deleteDatabase();
    }

    @Test
    protected void equals_shouldReturnTrue_forSameLanguage() throws IOException {
        assertEquals(LanguagesEnum.DEU, Configurations.getLanguage());
    }

    @Test
    protected void equals_shouldReturnTrue_forSameTheme() throws IOException {
        assertEquals(ThemesEnum.CARBON, Configurations.getTheme());
    }

    private void buildAndReloadConfigurations() throws IOException {
        buildValidConfigurationsObject();
        realodConfigurations();
    }

    private void buildValidConfigurationsObject() {
        Configurations.setLanguage(LanguagesEnum.DEU);
        Configurations.setTheme(ThemesEnum.CARBON.getThemeName());
    }

    private void realodConfigurations() {
        Configurations.updateAllConfigurations();
        Configurations.loadAllConfigurations();
    }
}
