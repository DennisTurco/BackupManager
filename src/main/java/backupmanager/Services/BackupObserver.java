package backupmanager.Services;

import java.util.List;
import java.util.Map;
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
