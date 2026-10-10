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
        if (Arrays.asList(args).contains("--exit-with-parent")) exitWithParent();
        runApiServer();
    }

    // Electron passes --exit-with-parent and keeps our stdin open: it reaches EOF when the Electron
    // process ends in any way (quit, crash, killed from the task manager), so the backend never
    // outlives the app as an orphan that keeps the JAR locked and port 7089 taken. System.exit
    // also runs the shutdown hooks, unlike the hard kill that is the only option on Windows.
    private static void exitWithParent() {
        Thread watcher = new Thread(() -> {
            try {
                while (System.in.read() != -1) {
                    // ignore any input, only the end of the stream matters
                }
            } catch (IOException ignored) {
                // a broken pipe means the parent is gone as well
            }
            logger.info("Parent process ended, shutting down");
            System.exit(0);
        }, "Parent-Watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void dataInit() {
        ensureLogDirectory();
        ConfigKey.loadFromJson(CONFIG);

        databaseInitialization();

        LanguageManager.initialize();
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
