package backupmanager;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Controllers.AppController;
import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Database;
import backupmanager.database.DatabaseInitializer;
import backupmanager.database.DatabasePaths;

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        ConfigKey.loadFromJson(CONFIG);

        databaseInitialization();

        loadPreferredLanguage();

        boolean isBackgroundMode = isBackgroundMode(args);

        logger.info("Application started");
        logger.debug("Background mode: {}", isBackgroundMode);

        if (isBackgroundMode) {
            runBackgroundProcess();
        }
        else if (!isBackgroundMode) {
            runGui();
        }
    }

    private static void databaseInitialization() {
        try {
            Database.init(DatabasePaths.getDatabasePath());
            DatabaseInitializer.init();
        } catch (Exception ex) {
            logger.error("Unable to init the database");
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void loadPreferredLanguage() {
        try {
            Preferences.loadAllPreferences();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: {}", ex.getMessage(), ex);
        }
    }

    private static boolean isBackgroundMode(String[] args) {
        boolean isBackgroundMode = args.length > 0 && args[0].equalsIgnoreCase("--background");

        if (!isBackgroundMode && args.length > 0) {
            logger.error("Argument \"{}\" not valid!", args[0]);
            throw new IllegalArgumentException("Argument passed is not valid!");
        }

        return isBackgroundMode;
    }

    private static void runBackgroundProcess() {
        logger.info("Backup service starting in the background");
        try {
            AppController.startBackgroundProcess();
        } catch (IOException ex) {
            logger.error("An error occurred: {}", ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void runGui() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            BackupManagerGUI gui = new BackupManagerGUI();
            gui.showWindow();
        });
    }
}
