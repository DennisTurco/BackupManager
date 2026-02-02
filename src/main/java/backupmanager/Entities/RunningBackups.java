package backupmanager.Entities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import backupmanager.BackupOperations;
import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Enums.ConfigKey;

// this class contains only the RunningBackups entity
// this entity is used to store the information of the backups that are currently running
// i use this object to know wich backups are currently running across the instances
public class RunningBackups {
    private static final Logger logger = LoggerFactory.getLogger(RunningBackups.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String name;
    private final String path;
    private int progress;
    private BackupStatusEnum status;

    public RunningBackups(String name, String path, int progress, BackupStatusEnum status) {
        this.name = name;
        this.path = path;
        this.progress = progress;
        this.status = status;
    }

    private static File getBackupFile() {
        return new File(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.RUNNING_BACKUPS_FILE_STRING.getValue());
    }

    public static synchronized List<RunningBackups> readBackupListFromJSON() {
        File file = getBackupFile();
        int attempts = 5;

        for (int i = 0; i < attempts; i++) {
            try {
                if (!file.exists() || file.length() == 0) {
                    logger.warn("The backup file does not exist or is empty. Attempt " + (i + 1) + "/" + attempts);
                    Thread.sleep(new Random().nextInt(100, 150));
                    continue;
                }

                return objectMapper.readValue(file, new TypeReference<List<RunningBackups>>() {});
            } catch (IOException e) {
                logger.error("Error while reading the file: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return new ArrayList<>();
    }

    public static RunningBackups readBackupFromJSON(String name) {
        List<RunningBackups> backups = readBackupListFromJSON();

        if (backups == null || backups.isEmpty()) return null;

        for (RunningBackups backup : backups) {
            if (backup.name.equals(name)) {
                return backup;
            }
        }
        return null; // Return null if no backup with the specified name is found
    }

    // the system is multi threading, it is possible that multiple threads call this method, so i need to use synchronized keyworl
    public static synchronized void updateBackupToJSON(RunningBackups backup) {
        List<RunningBackups> backups = readBackupListFromJSON();
        boolean updated = false;

        // Iterate through existing backups to update
        for (ListIterator<RunningBackups> iterator = backups.listIterator(); iterator.hasNext(); ) {
            RunningBackups currentBackup = iterator.next();
            if (currentBackup.name.equals(backup.name)) {

                if (backup.progress == 100) {
                    backup.status = BackupStatusEnum.Finished;
                } else if (backup.status != null && backup.status != BackupStatusEnum.Terminated) {
                    backup.status =  BackupStatusEnum.Progress;
                } else {
                    backup.status =  BackupStatusEnum.Terminated;
                }

                logger.debug("Backup '{}' updated with the status: {}", backup.name, backup.status);

                iterator.set(backup);
                updated = true;
                break;
            }
        }

        // If the backup wasn't found in the list, add it
        if (!updated && backup.progress != 100) {
            backup.status = BackupStatusEnum.Progress;
            backups.add(backup);

            logger.info("Backup '{}' created with the status: {}", backup.name, backup.status);
        }

        updateBackupsToJSON(backups);
    }

    public static synchronized void updateBackupStatusAfterCompletition(String name) {
        List<RunningBackups> backups = readBackupListFromJSON();
        boolean updated = false;
        BackupStatusEnum status = BackupStatusEnum.Finished;

        for (RunningBackups backup : backups) {
            if (backup.name.equals(name)) {
                if (backup.progress == 100) {
                    backup.status = status;
                } else {
                    status = BackupStatusEnum.Terminated;
                    backup.status = status;
                    cleanRunningBackupsFromJSON(name); // delete partial backup
                }

                updated = true;
                break;
            }
        }

        if (updated) {
            updateBackupsToJSON(backups);
            logger.info("Backup '{}' updated with the status: {}", name, status);
        } else {
            logger.warn("Backup '{}' didn't find. No status update", name);
        }
    }

    private static synchronized void updateBackupsToJSON(List<RunningBackups> backups) {
        File file = getBackupFile();
        int attempts = 5;

        for (int i = 0; i < attempts; i++) {
            try {
                objectMapper.writeValue(file, backups);
                return;
            } catch (IOException e) {
                logger.warn("Attempt " + (i + 1) + " to write failed: " + e.getMessage());
                try {
                    Thread.sleep(new Random().nextInt(100, 150));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        logger.error("Error: unable to write to JSON after " + attempts + " attempts.");
    }

    public static synchronized void cleanRunningBackupsFromJSON(String name) {
        List<RunningBackups> backups = readBackupListFromJSON();
        backups.removeIf(runningBackup ->
            (runningBackup.progress != 100 && BackupOperations.deletePartialBackup(runningBackup.path)) ||
            (runningBackup.progress == 100 && runningBackup.name.equals(name))
        );

        updateBackupsToJSON(backups);
    }

    public static synchronized void deleteCompletedBackup(String name) {
        List<RunningBackups> backups = readBackupListFromJSON();
        backups.removeIf(backup -> backup.name.equals(name) && (backup.status == BackupStatusEnum.Finished || backup.status == BackupStatusEnum.Terminated));

        updateBackupsToJSON(backups);
    }

    public static synchronized void deleteCompletedBackups() {
        List<RunningBackups> backups = readBackupListFromJSON();
        backups.removeIf(backup -> backup.status == BackupStatusEnum.Finished || backup.status == BackupStatusEnum.Terminated);

        updateBackupsToJSON(backups);
    }

    // remove all backups. I don't care the status, we have to delete everything
    public static synchronized void deletePartialBackupsStuckedJSONFile() {
        List<RunningBackups> backups = readBackupListFromJSON();
        backups.removeIf(backup -> BackupOperations.deletePartialBackup(backup.path));

        updateBackupsToJSON(backups);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getProgress() {
        return progress;
    }

    public BackupStatusEnum getStatus() {
        return status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setStatus(BackupStatusEnum status) {
        this.status = status;
    }
}
