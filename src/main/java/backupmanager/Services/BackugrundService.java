package backupmanager.Services;

import java.awt.*;
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
 
import backupmanager.Entities.Backup;
import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Json.JSONAutoBackup;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Logger;
import backupmanager.BackupOperations;
import backupmanager.Entities.RunningBackups;

public class BackugrundService {
    private ScheduledExecutorService scheduler;
    private final JSONAutoBackup json = new JSONAutoBackup();
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
        Logger.logMessage("Stopping background service", Logger.LogLevel.DEBUG);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            Logger.logMessage("Background service stopped", Logger.LogLevel.INFO);
        }
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    private void createHiddenIcon() {
        if (!SystemTray.isSupported()) {
            Logger.logMessage("System tray is not supported!", Logger.LogLevel.WARN);
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
            Logger.logMessage("TrayIcon added", Logger.LogLevel.INFO);
        } catch (AWTException e) {
            Logger.logMessage("TrayIcon could not be added", Logger.LogLevel.ERROR, e);
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
        Logger.logMessage("Showing the GUI", Logger.LogLevel.INFO);
        
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
            Logger.logMessage("Checking for automatic backup...", Logger.LogLevel.INFO);
            try {
                List<Backup> backups = json.readBackupListFromJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile());
                List<Backup> needsBackup = getBackupsToDo(backups);
                if (needsBackup != null && !needsBackup.isEmpty()) {
                    Logger.logMessage("Start backup process.", Logger.LogLevel.INFO);
                    executeBackups(needsBackup);
                } else {
                    Logger.logMessage("No backup needed at this time.", Logger.LogLevel.INFO);
                }
            } catch (IOException ex) {
                Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            }
        }

        private List<Backup> getBackupsToDo(List<Backup> backups) {
            List<Backup> backupsToDo = new ArrayList<>();
            for (Backup backup : backups) {
                if (backup.getNextDateBackup() != null && backup.getNextDateBackup().isBefore(LocalDateTime.now())) {
                    backupsToDo.add(backup);
                }
            }
            return backupsToDo;
        }

        private void executeBackups(List<Backup> backups) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                for (Backup backup : backups) {
                    BackupOperations.SingleBackup(backup, trayIcon, null, null, null, null, null, null);
                }
            });
        }
    }
}
