package backupmanager.Controllers;

import java.awt.Frame;
import java.io.IOException;
import java.time.LocalDate;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Preferences;
import backupmanager.Entities.Subscription;
import backupmanager.Enums.ConfigKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Helpers.SubscriptionNotifier;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Services.BackgroundService;
import backupmanager.database.Repositories.SubscriptionRepository;

public class AppController {
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private BackupManagerGUI guiInstance;

    private final BackgroundService backgroundService;
    private final TrayController trayController;

    public static AppController startBackgroundProcess() throws IOException {
        return new AppController();
    }

    private AppController() throws IOException {
        logger.info("Starting RemindMe application");

        this.backgroundService = new BackgroundService();

        this.trayController = new TrayController(
            this::openGui,
            this::exitApp
        );

        trayController.start();

        if (canBackgroundServiceStartsBasedOnSubscription())
            backgroundService.start(this.trayController);
    }

    private boolean canBackgroundServiceStartsBasedOnSubscription() {
        if (!Preferences.isSubscriptionNedded()) return true;

        Subscription subscription = SubscriptionRepository.getAnySubscriptionValid();

        if (subscription == null) {
            SubscriptionNotifier.showExpiredAlert(trayController);
            return false;
        }

        int days = configReader.getConfigValue("SubscriptionWarningDays", 7);
        LocalDate now = LocalDate.now();
        LocalDate endMinusDays = subscription.endDate().minusDays(days);

        if (now.isAfter(endMinusDays)) {
            SubscriptionNotifier.showExpiringWarning(trayController);
        }

        return true;
    }

    private void openGui() {
        logger.info("Opening main GUI");

        if (guiInstance == null) {
            guiInstance = new BackupManagerGUI();
            guiInstance.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }

        guiInstance.setVisible(true);
        guiInstance.toFront();
        guiInstance.requestFocus();

        if (guiInstance.getState() == Frame.ICONIFIED) {
            guiInstance.setState(Frame.NORMAL);
        }
    }

    private void exitApp() {
        logger.info("Exiting application");

        backgroundService.stop();
        System.exit(0);
    }
}
