package backupmanager.Services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.gui.Controllers.TrayController;
import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

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

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Backup-Background-Service"));

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

            if (BackupRequestRepository.isAnyBackupRunning()) {
                logger.info("A backup is already running. Skipping this cycle.");
                isBackingUp.set(false);
                return;
            }

            Map<Integer, ConfigurationBackup> backupMap = BackupConfigurationRepository.getBackupMap();
            List<ConfigurationBackup> backupsToDo = getBackupsToDo(backupMap, 1);

            if (!backupsToDo.isEmpty()) {
                logger.info("Start backup process.");
                executeBackups(backupsToDo);
            } else {
                isBackingUp.set(false);
                logger.debug("No backup needed at this time.");
            }
        }

        private List<ConfigurationBackup> getBackupsToDo(Map<Integer, ConfigurationBackup> backupMap, int maxBackupsToAdd) {
            List<ConfigurationBackup> backupsToDo = new ArrayList<>();
            List<BackupRequest> running = BackupRequestRepository.getRunningBackups();

            for (ConfigurationBackup backup : backupMap.values()) {
                boolean alreadyRunning = running.stream()
                        .anyMatch(r -> r.backupConfigurationId() == backup.getId() &&
                                       r.status() == backupmanager.Enums.BackupStatus.IN_PROGRESS);

                if (!alreadyRunning
                        && maxBackupsToAdd > 0
                        && backup.isAutomatic()
                        && backup.getNextBackupDate() != null
                        && backup.getNextBackupDate().isBefore(LocalDateTime.now())) {
                    backupsToDo.add(backup);
                    maxBackupsToAdd--;
                }
            }
            return backupsToDo;
        }

        private void executeBackups(List<ConfigurationBackup> backups) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    for (ConfigurationBackup backup : backups) {
                        ZippingContext context = ZippingContext.create(backup, trayIcon.getTrayIcon(), null, null, null, null);
                        BackupOperations.singleBackup(context, BackupTriggerType.SCHEDULER);
                    }
                } finally {
                    logger.info("All backups completed. Resetting isBackingUp flag.");
                    isBackingUp.set(false);
                }
            });
        }
    }
}
