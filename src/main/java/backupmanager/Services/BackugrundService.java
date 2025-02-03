package backupmanager.Services;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import backupmanager.BackupOperations;
import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Entities.RunningBackups;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.ConfigKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Json.JSONBackup;
import backupmanager.Json.JSONConfigReader;

public class BackugrundService {
    private static final Logger logger = LoggerFactory.getLogger(BackugrundService.class);

    private ScheduledExecutorService scheduler;
    private final JSONBackup json = new JSONBackup();
    private final JSONConfigReader jsonConfig = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private TrayIcon trayIcon = null;
    private BackupManagerGUI guiInstance = null;

    public void startService() throws IOException {
        if (trayIcon == null) {
            createHiddenIcon();
        }
        
        // clear running backups json file (if last execution stopped brutally we have to delete the partial backups)
        RunningBackups.deletePartialBackupsStuckedJSONFile();
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long interval = jsonConfig.readCheckForBackupTimeInterval();
        scheduler.scheduleAtFixedRate(new BackupTask(), 0, interval, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopService));
    }

    public void stopService() {
        logger.debug("Stopping background service");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            logger.info("Background service stopped");
        }
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    private void createHiddenIcon() {
        if (!SystemTray.isSupported()) {
            logger.warn("System tray is not supported!");
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(ConfigKey.LOGO_IMG.getValue()));
        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent e) -> {
            stopService(); // close the backup service
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Backup Service", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            logger.info("TrayIcon added");
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added: " + e.getMessage(), e);
        }

        // Listener for click to tray icon
        trayIcon.addActionListener((ActionEvent e) -> {
            javax.swing.SwingUtilities.invokeLater(this::showMainGUI); // show the GUI
        });

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    showMainGUI(); // left button mouse
                }
            }
        });
    }

    private void showMainGUI() {
        logger.info("Showing the GUI");
        
        if (guiInstance == null) {
            guiInstance = new BackupManagerGUI();
            guiInstance.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            guiInstance.setVisible(true);
        } else {
            guiInstance.setVisible(true);
            guiInstance.toFront();
            guiInstance.requestFocus();
            if (guiInstance.getState() == Frame.ICONIFIED) {
                guiInstance.setState(Frame.NORMAL);
            }
        }
    }

    class BackupTask implements Runnable {
        @Override
        public void run() {
            logger.debug("Checking for automatic backup...");
            try {
                List<Backup> backups = json.readBackupListFromJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile());
                List<Backup> needsBackup = getBackupsToDo(backups, 1);
                if (needsBackup != null && !needsBackup.isEmpty()) {
                    logger.info("Start backup process.");
                    executeBackups(needsBackup);
                } else {
                    logger.debug("No backup needed at this time.");
                }
            } catch (IOException ex) {
                logger.error("An error occurred: " + ex.getMessage(), ex);
            }
        }

        private List<Backup> getBackupsToDo(List<Backup> backups, int maxBackupsToAdd) {
            List<Backup> backupsToDo = new ArrayList<>();
            List<RunningBackups> runningBackups = RunningBackups.readBackupListFromJSON();

            for (Backup backup : backups) {

                // i have to check that the backup is not running 
                boolean found = false;
                for (RunningBackups running : runningBackups) {
                    if (backup.getBackupName().equals(running.getBackupName())){
                        found = true;
                        break;
                    }
                }

                if (!found && maxBackupsToAdd > 0 && backup.isAutoBackup() && backup.getNextDateBackup() != null && backup.getNextDateBackup().isBefore(LocalDateTime.now())) {
                    backupsToDo.add(backup);
                    maxBackupsToAdd--;
                }
            }
            return backupsToDo;
        }

        private void executeBackups(List<Backup> backups) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                for (Backup backup : backups) {
                    ZippingContext context = new ZippingContext(backup, trayIcon, null, null, null, null);
                    BackupOperations.SingleBackup(context);
                }
            });
        }
    }
}
