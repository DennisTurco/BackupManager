package backupmanager;

import java.awt.Font;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.util.FontUtils;

import backupmanager.gui.Controllers.AppController;
import backupmanager.Entities.Confingurations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.ProductionDatabaseInitializer;
import backupmanager.gui.frames.BackupManager;
import backupmanager.utils.DemoPreferences;

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
            Database.init(DatabasePaths.getProductionDatabasePath());
            ProductionDatabaseInitializer.init();
        } catch (Exception ex) {
            logger.error("Unable to init the database");
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void loadPreferredLanguage() {
        try {
            Confingurations.loadAllConfigurations();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Confingurations.getLanguage().getFileName());
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
        try {
            AppController.startBackgroundProcess();
        } catch (IOException ex) {
            logger.error("An error occurred: {}", ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void runGui() {
        java.awt.EventQueue.invokeLater(() -> {

            DemoPreferences.init();
            FlatRobotoFont.install();
            FlatLaf.registerCustomDefaultsSource(".themes");
            UIManager.put("defaultFont", FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
            DemoPreferences.setupLaf();

            BackupManager frame = new BackupManager();
            frame.setVisible(true);
        });
    }
}
