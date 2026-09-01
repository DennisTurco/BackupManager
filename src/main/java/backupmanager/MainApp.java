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

import backupmanager.BackupOperations;
import backupmanager.Entities.Configurations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Managers.LanguageManager;
import backupmanager.Services.BackgroundService;
import backupmanager.Utils.AppPreferences;
import backupmanager.api.ApiServer;
import backupmanager.database.Database;
import backupmanager.database.DatabasePaths;
import backupmanager.database.ProductionDatabaseInitializer;
import backupmanager.gui.Controllers.AppController;
import backupmanager.gui.frames.BackupManager;

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        dataInit();

        String mode = parseMode(args);

        logger.info("Application started in mode: {}", mode);

        switch (mode) {
            case "background" -> runBackgroundProcess();
            case "api-server" -> runApiServer();
            default -> runGui();
        }
    }

    private static void dataInit() {
        ensureLogDirectory();
        ConfigKey.loadFromJson(CONFIG);

        databaseInitialization();

        AppPreferences.init();
        LanguageManager.loadPreferredLanguage();
        Configurations.loadAllConfigurations();
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

    private static String parseMode(String[] args) {
        if (args.length == 0) return "gui";
        return switch (args[0].toLowerCase()) {
            case "--background" -> "background";
            case "--api-server" -> "api-server";
            default -> {
                logger.error("Argument \"{}\" not valid!", args[0]);
                throw new IllegalArgumentException("Argument passed is not valid: " + args[0]);
            }
        };
    }

    private static void ensureLogDirectory() {
        try {
            String logDir = System.getProperty("user.home") + "/.backupmanager/logs";
            java.nio.file.Path path = java.nio.file.Paths.get(logDir);
            java.nio.file.Files.createDirectories(path);

            logger.info("Log directory ensured at: {}", path.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to create log directory", e);
            throw new RuntimeException("Cannot initialize log directory", e);
        }
    }

    private static void runBackgroundProcess() {
        try {
            AppController.startBackgroundProcess();
        } catch (IOException ex) {
            logger.error("An error occurred: {}", ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void runApiServer() {
        System.setProperty("java.awt.headless", "true");
        try {
            BackupOperations.deletePotentiallyIncompletedBackupsFromLastExecution();

            BackgroundService backgroundService = new BackgroundService();
            backgroundService.start();

            ApiServer apiServer = new ApiServer();
            apiServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                backgroundService.stop();
                apiServer.stop();
            }));

            Thread.currentThread().join();
        } catch (IOException ex) {
            logger.error("API server startup failed: {}", ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.info("API server process interrupted");
        }
    }

    private static void runGui() {
        java.awt.EventQueue.invokeLater(() -> {
            initLaf();
            BackupManager.getInstance().setVisible(true);
        });
    }

    public static void initLaf() {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource(".themes");
        UIManager.put(
            "defaultFont",
            FontUtils.getCompositeFont(FlatRobotoFont.FAMILY, Font.PLAIN, 13)
        );
        AppPreferences.setupLaf();
    }
}
