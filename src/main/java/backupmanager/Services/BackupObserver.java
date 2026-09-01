package backupmanager.Services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.Table.BackupTableDataService;

/*
 * I need a task that constantly checks if there are something running and i can't use a simple method calls instead because
 * if a backup starts caused by the BackugroundService and we open the GUI, thre are 2 different instance of this program,
 * so we need something like an observer that constantly checks if there are some backups in progress.
 */
public class BackupObserver {
    private static final Logger logger = LoggerFactory.getLogger(BackupObserver.class);

    private final ScheduledExecutorService scheduler;
    private final BackupTableDataService tableService;
    private final long millisecondsToWait;

    // track last-seen timestamps for backups that were running
    private final ConcurrentMap<Integer, Long> lastSeenRunning = new ConcurrentHashMap<>();

    // grace period to wait before removing progress indicator (milliseconds)
    private final long graceMillis = 3000L;

    public BackupObserver(BackupTableDataService tableService, int millisecondsToWait) {
        this.tableService = tableService;
        this.millisecondsToWait = millisecondsToWait;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(); // create single thread
    }

    public void start() {
        logger.info("Observer for running backups started");

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                List<BackupRequest> running = BackupRequestRepository.getRunningBackups();

                Map<Integer, ConfigurationBackup> configs = BackupConfigurationRepository.getBackupMap();

                // Collect running configuration ids
                Set<Integer> runningConfigIds = new HashSet<>();
                for (BackupRequest r : running) runningConfigIds.add(r.backupConfigurationId());

                // Update progress for running backups
                for (BackupRequest request : running) {

                    ConfigurationBackup config = configs.get(request.backupConfigurationId());

                    if (config == null)
                        continue;

                    SwingUtilities.invokeLater(() -> {

                        BackupRequest updatedRequest = BackupRequestRepository.getBackupRequestById(request.backupRequestId());

                        if (updatedRequest != null && updatedRequest.progress() < 99) {
                            tableService.updateProgress(config, updatedRequest.progress());
                        } else {
                            tableService.removeProgress(config);
                        }
                    });
                }

                long now = System.currentTimeMillis();

                // Update last seen timestamps for running backups
                for (BackupRequest r : running) {
                    lastSeenRunning.put(r.backupConfigurationId(), now);
                }

                // Cleanup any progress indicators for backups that are not currently running,
                // but only if we saw them running before and the grace period has elapsed.
                for (ConfigurationBackup config : configs.values()) {
                    int id = config.getId();
                    if (!runningConfigIds.contains(id)) {
                        Long lastSeen = lastSeenRunning.get(id);
                        if (lastSeen != null) {
                            if (now - lastSeen >= graceMillis) {
                                lastSeenRunning.remove(id);
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        tableService.removeProgress(config);
                                    } catch (Exception e) {
                                        logger.debug("Error while cleaning obsolete progress for {}: {}", config.getName(), e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                logger.error("Observer error", ex);
            }

        }, 0, millisecondsToWait, TimeUnit.MILLISECONDS); // run now and periodically
    }

    public void stop() {
        logger.info("Observer for running backups stopped");
        scheduler.shutdownNow();
    }
}
