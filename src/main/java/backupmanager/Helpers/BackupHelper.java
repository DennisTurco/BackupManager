package backupmanager.Helpers;

import java.awt.Component;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Exceptions.BackupDeletionException;
import backupmanager.Utils.ModalUtils;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.simple.TimePickerDialog;
import raven.modal.component.SimpleModalBorder;

public class BackupHelper {

    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH-mm-ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static void newBackup(ConfigurationBackup backup) {
        logger.info("Event --> new backup");
        BackupConfigurationRepository.insertBackup(backup);
    }

    public static boolean deleteBackupWithConfirmition(ConfigurationBackup backup) throws BackupDeletionException {
        logger.info("Event --> deleting backup request with confirmation for backup: " + backup.getName());

        int response = JOptionPane.showConfirmDialog(null, Translations.get(TKey.CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            return BackupHelper.deleteBackup(backup);
        }
        return false;
    }

    public static boolean deleteBackup(String backupName) throws BackupDeletionException {
        logger.info("Event --> deleting backup");
        ConfigurationBackup backup = ConfigurationBackup.getBackupByName(backupName);
        return deleteBackup(backup);
    }

    public static boolean deleteBackup(ConfigurationBackup backup) throws BackupDeletionException {
        logger.info("Event --> deleting backup" + backup.getName());
        BackupConfigurationRepository.deleteBackup(backup.getId());
        return true;
    }

    public static void updateBackup(ConfigurationBackup updatedBackup) {
        if (updatedBackup == null) {
            throw new IllegalArgumentException("Backup is null!");
        }

        if (updatedBackup.getId() != 0) {
            logger.info("Updating backup: " + updatedBackup.getName());
            BackupConfigurationRepository.updateBackup(updatedBackup);
        }
    }

    public static List<ConfigurationBackup> getBackupList() {
        List<ConfigurationBackup> backups = BackupConfigurationRepository.getBackupList();
        return backups;
    }

    public static TimeInterval openTimePicker() {
        return openTimePicker(new TimePickerDialog(null));
    }

    public static TimeInterval openTimePicker(TimePickerDialog picker) {
        picker.setVisible(true);
        return picker.getResult();
    }

    public static void showMessageActivationAutoBackup(Component parent, TimeInterval timeInterval, String startPath, String destinationPath) {
        String from = Translations.get(TKey.FROM);
        String to = Translations.get(TKey.TO);
        String activated = Translations.get(TKey.AUTO_BACKUP_ACTIVATED_MESSAGE);
        String setted = Translations.get(TKey.SETTED_EVERY_MESSAGE);
        String days = Translations.get(TKey.DAYS_MESSAGE);

        String message =
                activated + "\n\n" + from + ": " + startPath + "\n" + to + ": "
                + destinationPath + "\n" + setted + " " + timeInterval.toString() + days;

        ModalUtils.showInfo(parent, Translations.get(TKey.AUTO_BACKUP_MESSAGE), message, SimpleModalBorder.CLOSE_OPTION);
    }

    public static LocalDateTime getNexDateBackup(TimeInterval timeInterval) {
        return LocalDateTime.now()
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static void forceBackupTermination(BackupRequest request) {
        BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatus.TERMINATED);
        deletePartialBackup(request.outputPath());
    }

    public static ConfigurationBackup toggleAutomaticBackup(Component parent, ConfigurationBackup backup) {
        logger.info("Event --> automatic backup");

        if (backup.isAutomatic()) {
            int response = JOptionPane.showConfirmDialog(null, Translations.get(TKey.CONFIRMATION_MESSAGE_CANCEL_AUTO_BACKUP), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return null;
            }

            backup.setAutomatic(false);
            backup.setTimeIntervalBackup(null);
            backup.setNextBackupDate(null);
            backup.setLastUpdateDate(LocalDateTime.now());

            logger.info("Automatic backup turned off");

            updateBackup(backup);

            return backup;
        }

        if(!BackupOperations.checkInputCorrect(backup.getName(), backup.getTargetPath(), backup.getDestinationPath(), null))
            return null;

        // if the file has not been saved you need to save it before setting the auto backup
        if(!backup.isAutomatic() || backup.getNextBackupDate() == null || backup.getTimeIntervalBackup() == null) {
            if (backup.getName() == null || backup.getName().isEmpty()) return null;

            // message
            TimeInterval timeInterval = openTimePicker();
            if (timeInterval == null) return null;

            //set date for next backup
            LocalDateTime nextDateBackup = getNexDateBackup(timeInterval);

            backup.setAutomatic(true);
            backup.setTimeIntervalBackup(timeInterval);
            backup.setNextBackupDate(nextDateBackup);
            backup.setLastUpdateDate(LocalDateTime.now());

            logger.info("Automatic backup turned On and next date backup setted to {}", nextDateBackup);

            showMessageActivationAutoBackup(parent, timeInterval, backup.getTargetPath(), backup.getDestinationPath());

            updateBackup(backup);

            return backup;
        }

        return null;
    }

    private static boolean deletePartialBackup(String filePath) {
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
