package backupmanager.Controllers;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Services.BackgroundService;

public class AppController {

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

        backgroundService.start(this.trayController);
        trayController.start();
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
