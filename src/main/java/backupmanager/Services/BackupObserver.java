package backupmanager.Services;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Backup;
import backupmanager.Entities.RunningBackups;
import backupmanager.Table.TableDataManager;

/*
 * I need a task that constantly checks if there are something running and i can't use a simple method calls instead because
 * if a backup starts caused by the BackugroundService and we open the GUI, thre are 2 different instance of this program, 
 * so we need something like an observer that constantly checks if there are some backups in progress.
 */
public class BackupObserver {
    private static final Logger logger = LoggerFactory.getLogger(BackupObserver.class);

    private final ScheduledExecutorService scheduler;
    private final DateTimeFormatter formatter;
    private final long millisecondsToWait;

    public BackupObserver(DateTimeFormatter formatter, int millisecondsToWait) {
        this.millisecondsToWait = millisecondsToWait;
        this.formatter = formatter;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(); // create single thread
    }
    
    public void start() {
        logger.info("Observer for running backups started");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<RunningBackups> runningBackups = RunningBackups.readBackupListFromJSON();
                if (!runningBackups.isEmpty()) {
                    logger.debug("Observer has found a running backup");

                    for (RunningBackups backup : runningBackups) {
                        Backup backupEntity = Backup.getBackupByName(backup.getBackupName());

                        int value = backup.getProgress();
                        if (value < 100) {
                            TableDataManager.updateProgressBarPercentage(backupEntity, value, formatter);
                        } else {
                            TableDataManager.removeProgressInTheTableAndRestoreAsDefault(backupEntity, formatter);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("An error occurred: " + ex.getMessage(), ex);
            }
        }, 0, millisecondsToWait, TimeUnit.MILLISECONDS); // run now and periodically
    }

    public void stop() {
        logger.info("Observer for running backups stopped");
        scheduler.shutdownNow(); 
    }
}
