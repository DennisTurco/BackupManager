package backupmanager.Services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.BackupExecutionContext;
import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import backupmanager.Json.JsonConfig;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackgroundService {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundService.class);

    private ScheduledExecutorService scheduler;

    private final JsonConfig jsonConfig = JsonConfig.getInstance();
    private final AtomicBoolean isBackingUp = new AtomicBoolean(false);

    public void start() throws IOException {
        if (isRunning()) {
            logger.warn("BackgroundService already running");
            return;
        }

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
            // An exception escaping a scheduleWithFixedDelay task cancels every future run, which
            // would silently stop all automatic backups until the app restarts
            try {
                checkForBackups();
            } catch (Throwable t) {
                isBackingUp.set(false);
                logger.error("Automatic backup check failed: {}", t.getMessage(), t);
            }
        }

        private void checkForBackups() {
            // Checked on every cycle so that an expiry or a renewal applies without restarting
            if (SubscriptionHelper.getSubscriptionStatus() == SubscriptionStatus.EXPIRED) {
                logger.debug("Subscription expired, automatic backups paused");
                return;
            }

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
            List<ConfigurationBackup> backupsToDo = getBackupsToDo(backupMap);

            if (!backupsToDo.isEmpty()) {
                logger.info("Start backup process.");
                executeBackups(backupsToDo);
            } else {
                isBackingUp.set(false);
                logger.debug("No backup needed at this time.");
            }
        }

        // All due backups, most overdue first. Only one can run at a time; executeBackups starts the
        // first one that is actually able to run, so a broken backup can't starve the others.
        private List<ConfigurationBackup> getBackupsToDo(Map<Integer, ConfigurationBackup> backupMap) {
            List<ConfigurationBackup> backupsToDo = new ArrayList<>();
            List<BackupRequest> running = BackupRequestRepository.getRunningBackups();

            for (ConfigurationBackup backup : backupMap.values()) {
                boolean alreadyRunning = running.stream()
                        .anyMatch(r -> r.backupConfigurationId() == backup.getId() &&
                                       r.status() == backupmanager.Enums.BackupStatus.IN_PROGRESS);

                if (!alreadyRunning
                        && backup.isAutomatic()
                        && backup.getNextBackupDate() != null
                        && backup.getNextBackupDate().isBefore(LocalDateTime.now())) {
                    backupsToDo.add(backup);
                }
            }
            backupsToDo.sort(Comparator.comparing(ConfigurationBackup::getNextBackupDate));
            return backupsToDo;
        }

        private void executeBackups(List<ConfigurationBackup> backups) {
            try {
                for (ConfigurationBackup backup : backups) {
                    if (BackupOperations.isBackupInFlight()) break; // the next cycle picks up the rest
                    boolean started = false;
                    try {
                        ZippingContext context = new ZippingContext(BackupExecutionContext.create(backup));
                        started = BackupOperations.requestSingleBackup(context, BackupTriggerType.SCHEDULER);
                    } catch (RuntimeException e) {
                        // e.g. the source folder is gone (unplugged drive)
                        logger.warn("Automatic backup \"{}\" skipped: {}", backup.getName(), e.getMessage());
                    }
                    if (started) break;
                    // Couldn't start (missing path, …): retry later instead of blocking the queue every cycle
                    BackupOperations.postponeAfterFailedRun(backup.getId());
                }
            } finally {
                logger.info("All backups completed. Resetting isBackingUp flag.");
                isBackingUp.set(false);
            }
        }
    }
}
