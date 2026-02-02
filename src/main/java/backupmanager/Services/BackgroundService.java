package backupmanager.Services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Controllers.TrayController;
import backupmanager.Entities.Backup;
import backupmanager.Entities.RunningBackups;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Repositories.BackupConfigurationRepository;

public class BackgroundService {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundService.class);

    private ScheduledExecutorService scheduler;

    private TrayController trayIcon;
    private final JSONConfigReader jsonConfig = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private final AtomicBoolean isBackingUp = new AtomicBoolean(false);

    public void start(TrayController trayIcon) throws IOException {
        if (isRunning()) {
            logger.warn("BackgroundService already running");
            return;
        }

        this.trayIcon = trayIcon;

        // clear running backups json file (if last execution stopped brutally we have to delete the partial backups)
        RunningBackups.deletePartialBackupsStuckedJSONFile();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Remind-Background-Service"));

        long interval = jsonConfig.readCheckForBackupTimeInterval();

        scheduler.scheduleWithFixedDelay(new BackupTask(), 0, interval, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        logger.info("BackgroundService started");
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            logger.info("BackgroundService stopped");
        }
    }

    private boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    private class BackupTask implements Runnable {
        @Override
        public void run() {
            if (!isBackingUp.compareAndSet(false, true)) {
                return;
            }

            logger.debug("Checking for automatic backup...");

            List<RunningBackups> runningBackups = RunningBackups.readBackupListFromJSON();
            if (!runningBackups.isEmpty()) {
                logger.info("A backup is already running. Skipping this cycle.");
                isBackingUp.set(false);
                return;
            }

            List<Backup> backups = BackupConfigurationRepository.getBackupList();
            List<Backup> needsBackup = getBackupsToDo(backups, 1);
            if (!needsBackup.isEmpty()) {
                logger.info("Start backup process.");
                executeBackups(needsBackup);
            } else {
                isBackingUp.set(false);
                logger.debug("No backup needed at this time.");
            }
        }

        private List<Backup> getBackupsToDo(List<Backup> backups, int maxBackupsToAdd) {
            List<Backup> backupsToDo = new ArrayList<>();
            List<RunningBackups> runningBackups = RunningBackups.readBackupListFromJSON();

            for (Backup backup : backups) {

                // i have to check that the backup is not running
                boolean found = false;
                for (RunningBackups running : runningBackups) {
                    if (backup.getName().equals(running.getName())){
                        found = true;
                        break;
                    }
                }

                if (!found && maxBackupsToAdd > 0 && backup.isAutomatic() && backup.getNextBackupDate() != null && backup.getNextBackupDate().isBefore(LocalDateTime.now())) {
                    backupsToDo.add(backup);
                    maxBackupsToAdd--;
                }
            }
            return backupsToDo;
        }

        private void executeBackups(List<Backup> backups) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    for (Backup backup : backups) {
                        ZippingContext context = new ZippingContext(backup, trayIcon.geTrayIcon(), null, null, null, null);
                        BackupOperations.SingleBackup(context);
                    }
                } finally {
                    logger.info("All backups completed. Resetting isBackingUp flag.");
                    isBackingUp.set(false);
                }
            });
        }
    }
}
