package com.mycompany.autobackupprogram;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class BackupService {
    private ScheduledExecutorService scheduler;
    private final JSONAutoBackup json = new JSONAutoBackup();
    private final JSONConfigReader jsonConfig = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.RES_DIRECTORY_STRING.getValue());
    private TrayIcon trayIcon = null;
    private BackupManagerGUI guiInstance = null;

    public void startService() throws IOException {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long interval = jsonConfig.readCheckForBackupTimeInterval();
        scheduler.scheduleAtFixedRate(new BackupTask(), 0, interval, TimeUnit.MINUTES);

//        if (trayIcon == null) {
//            createHiddenIcon();
//        }

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
            stopService();
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Backup Service", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.logMessage("TrayIcon could not be added", Logger.LogLevel.ERROR, e);
        }

        trayIcon.addActionListener((ActionEvent e) -> {
            javax.swing.SwingUtilities.invokeLater(this::showMainGUI);
        });
    }

    private void showMainGUI() {
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
                List<Backup> backups = json.ReadBackupListFromJSON(ConfigKey.BACKUP_FILE_STRING.getValue(), ConfigKey.RES_DIRECTORY_STRING.getValue());
                List<Backup> needsBackup = getBackupsToDo(backups);
                if (needsBackup != null && !needsBackup.isEmpty()) {
                    Logger.logMessage("Start backup process.", Logger.LogLevel.INFO);
                    openMainGUI(needsBackup);
                } else {
                    Logger.logMessage("No backup needed at this time.", Logger.LogLevel.INFO);
                }
            } catch (IOException ex) {
                Logger.logMessage("An error occurred", Logger.LogLevel.ERROR, ex);
                ex.printStackTrace();
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

        private void openMainGUI(List<Backup> backups) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                showMainGUI();
                for (Backup backup : backups) {
                    BackupManagerGUI.currentBackup = backup;
                    guiInstance.SingleBackup(backup);
                }
            });
        }
    }
}