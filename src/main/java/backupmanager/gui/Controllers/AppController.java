package backupmanager.gui.Controllers;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import backupmanager.Helpers.SubscriptionNotifier;
import backupmanager.Services.BackgroundService;
import backupmanager.gui.frames.BackupManagerGUI;

public class AppController {
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private BackupManagerGUI guiInstance;

    private final BackgroundService backgroundService;
    private final TrayController trayController;

    public static AppController startBackgroundProcess() throws IOException {
        return new AppController();
    }

    private AppController() throws IOException {
        logger.info("Starting BackupManager application");

        this.backgroundService = new BackgroundService();

        this.trayController = new TrayController(
            this::openGui,
            this::exitApp
        );

        BackupOperations.deletePotentiallyIncompletedBackupsFromLastExecution();

        trayController.start();

        if (canBackgroundServiceStartsBasedOnSubscription()) {
            logger.info("Backup service starting in the background");
            backgroundService.start(this.trayController);
        }
    }

    private boolean canBackgroundServiceStartsBasedOnSubscription() {
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        showSubscriptionNotificationIfNeeded(status);
        return status != SubscriptionStatus.EXPIRED;
    }

    private void showSubscriptionNotificationIfNeeded(SubscriptionStatus status) {
        switch (status) {
            case SubscriptionStatus.EXPIRATION -> {
                logger.info("Subscription is expiring alert");
                SubscriptionNotifier.showExpiringWarning(trayController);
            }
            case SubscriptionStatus.EXPIRED -> {
                logger.info("Subscription expired alert");
                SubscriptionNotifier.showExpiredAlert(trayController);
            }
            case ACTIVE, NONE -> { }
        }
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
