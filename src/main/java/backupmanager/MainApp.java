package backupmanager;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Repositories.Repository;
import backupmanager.Services.BackugrundService;

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        ConfigKey.loadFromJson(CONFIG);

        try {
            Repository.initDatabaseIfNotExists();
        } catch (Exception ex) {
            logger.error("Unable to init the database");
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }

        try {
            Preferences.loadAllPreferences();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: {}", ex.getMessage(), ex);
        }

        boolean isBackgroundMode = args.length > 0 && args[0].equalsIgnoreCase("--background");

        if (!isBackgroundMode && args.length > 0) {
            logger.error("Argument \"{}\" not valid!", args[0]);
            throw new IllegalArgumentException("Argument passed is not valid!");
        }

        logger.info("Application started");
        logger.debug("Background mode: {}", isBackgroundMode);

        if (isBackgroundMode) {
            logger.info("Backup service starting in the background");
            BackugrundService service = new BackugrundService();
            try {
                service.startService();
            } catch (IOException ex) {
                logger.error("An error occurred: {}", ex.getMessage(), ex);
                ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }
        }
        else if (!isBackgroundMode) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                BackupManagerGUI gui = new BackupManagerGUI();
                gui.showWindow();
            });
        }
    }
}
