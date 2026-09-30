package backupmanager.Helpers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.BackupStatus;
import backupmanager.Exceptions.BackupDeletionException;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackupHelper {

    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH-mm-ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static void newBackup(ConfigurationBackup backup) {
        logger.info("Event --> new backup");
        BackupConfigurationRepository.insertBackup(backup);
    }

    public static boolean deleteBackup(String backupName) throws BackupDeletionException {
        logger.info("Event --> deleting backup");
        ConfigurationBackup backup = ConfigurationBackup.getBackupByName(backupName);
        return deleteBackup(backup);
    }

    public static boolean deleteBackup(ConfigurationBackup backup) throws BackupDeletionException {
        logger.info("Event --> deleting backup: {}", backup.getName());
        BackupConfigurationRepository.deleteBackup(backup.getId());
        return true;
    }

    public static void updateBackup(ConfigurationBackup updatedBackup) {
        if (updatedBackup == null) {
            throw new IllegalArgumentException("Backup is null!");
        }

        if (updatedBackup.getId() != 0) {
            logger.info("Updating backup: {}", updatedBackup.getName());
            BackupConfigurationRepository.updateBackup(updatedBackup);
        }
    }

    public static LocalDateTime getNexDateBackup(TimeInterval timeInterval) {
        return LocalDateTime.now()
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static void forceBackupTermination(BackupRequest request) {
        markTerminated(request);
        deletePartialBackup(request.outputPath());
    }

    // DB-status-only variant: used when the output file may still be open (e.g. a live
    // in-process interrupt), where deleting it here would fail on Windows (locked handle).
    // The caller is responsible for deleting the file once it's safely closed.
    public static void markTerminated(BackupRequest request) {
        BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatus.TERMINATED);
    }

    public static boolean deletePartialBackup(String filePath) {
        logger.info("Attempting to delete partial backup: " + filePath);

        if (filePath == null || filePath.isEmpty()) {
            logger.warn("The file path is null or empty.");
            return false;
        }

        File file = new File(filePath);

        // Check if the file exists and is a valid file
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    if (file.delete()) {
                        logger.info("Partial backup deleted successfully: " + file.getName());
                        return true;
                    } else {
                        logger.warn("Failed to delete partial backup (delete failed): " + file.getName());
                    }
                } catch (SecurityException e) {
                    logger.error("Security exception occurred while attempting to delete: " + file.getName(), e);
                } catch (Exception e) {
                    logger.error("Unexpected error while attempting to delete: " + file.getName(), e);
                }
            } else {
                logger.warn("The path points to a directory, not a file: " + filePath);
            }
        } else {
            logger.warn("The file does not exist: " + filePath);
        }

        return false;
    }
}
