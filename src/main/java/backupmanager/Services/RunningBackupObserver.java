package backupmanager.Services;

import java.time.format.DateTimeFormatter;
import java.util.List;

import backupmanager.Entities.Backup;
import backupmanager.Entities.RunningBackups;
import backupmanager.Logger;
import backupmanager.Table.BackupTable;
import backupmanager.Table.TableDataManager;

/*
 * I need a thread that constantly checks if there are something running and i can't use a simple method calls instead because
 * if a backup starts caused by the BackugroundService and we open the GUI, thre are 2 different instance of this program, 
 * so we need something like an observer that constantly checks if there are some backups in progress.
 */
public class RunningBackupObserver implements Runnable {

    private Thread thread;
    private final BackupTable backupTable;
    private final DateTimeFormatter formatter;
    private final long millisecondsToWait;

    public RunningBackupObserver(BackupTable backupTable, DateTimeFormatter formatter, int millisecondsToWait) {
        this.millisecondsToWait = millisecondsToWait;
        this.backupTable = backupTable;
        this.formatter = formatter;
    }
    
    @Override
    public void run() {
        Logger.logMessage("Observer for running backups started", Logger.LogLevel.INFO);

        try {
            while (thread != null && !thread.isInterrupted()) {
                List<RunningBackups> runningBackups = RunningBackups.readBackupListFromJSON();
                if (!runningBackups.isEmpty()) {
                    Logger.logMessage("Observer has found a running backup", Logger.LogLevel.DEBUG);

                    for (RunningBackups backup : runningBackups) {
                        Backup backupEntity = Backup.getBackupByName(backup.getBackupName());

                        int value = backup.getProgress();
                        if (value < 100) {
                            TableDataManager.updateProgressBarPercentage(backupTable, backupEntity, value, formatter);
                        } else if (value == 100) {
                            TableDataManager.removeProgressInTheTableAndRestoreAsDefault(backupEntity, backupTable, formatter);
                        }
                    }
                }

                Thread.sleep(millisecondsToWait);
            }
        } catch (InterruptedException ex) {
            Logger.logMessage("Observer thread was interrupted: " + ex.getMessage(), Logger.LogLevel.INFO, ex);
            // Clean up or exit gracefully
            Thread.currentThread().interrupt(); // Preserve the interrupted status
        } catch (Exception ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            ex.printStackTrace();
        } finally {
            Logger.logMessage("Observer for running backups stopped", Logger.LogLevel.INFO);
            this.stop();
        }
    }

    // Start the observer thread
    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

    // Stop the observer thread
    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null; // Ensure the thread reference is cleared
        }
        Logger.logMessage("Observer for running backups stopped explicitly", Logger.LogLevel.INFO);
    }

}
