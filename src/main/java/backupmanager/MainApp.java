package backupmanager;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        dataInit();

        logger.info("Starting API server");
        runApiServer();
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

    private static void runApiServer() {
        System.setProperty("java.awt.headless", "true");
        try {
            BackupOperations.deletePotentiallyIncompletedBackupsFromLastExecution();

            // Automatic backups run unless a subscription is required and none is valid (checked by
            // the scheduler on every cycle) — manual backups via the REST API stay available either way.
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
}
