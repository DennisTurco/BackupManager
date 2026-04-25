package backupmanager.Helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.simple.TimePickerDialog;

public class BackupHelper {

    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH-mm-ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static void newBackup(ConfigurationBackup backup) {
        logger.info("Event --> new backup");
        BackupConfigurationRepository.insertBackup(backup);
    }

    public static void deleteBackup(String backupName) {
        logger.info("Event --> deleting backup");
        ConfigurationBackup backup = ConfigurationBackup.getBackupByName(backupName);
        deleteBackup(backup);
    }

    public static void deleteBackup(ConfigurationBackup backup) {
        logger.info("Event --> deleting backup" + backup.getName());
        BackupConfigurationRepository.deleteBackup(backup.getId());
    }

    public static void deleteBackupWithConfirmition(ConfigurationBackup backup) {
        logger.info("Event --> deleting backup request with confirmation for backup: " + backup.getName());

        int response = JOptionPane.showConfirmDialog(null, Translations.get(TKey.CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            BackupHelper.deleteBackup(backup);
        }
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

    public static void showMessageActivationAutoBackup(TimeInterval timeInterval, String startPath, String destinationPath) {
        String from = Translations.get(TKey.FROM);
        String to = Translations.get(TKey.TO);
        String activated = Translations.get(TKey.AUTO_BACKUP_ACTIVATED_MESSAGE);
        String setted = Translations.get(TKey.SETTED_EVERY_MESSAGE);
        String days = Translations.get(TKey.DAYS_MESSAGE);

        JOptionPane.showMessageDialog(null,
                activated + "\n\t" + from + ": " + startPath + "\n\t" + to + ": "
                + destinationPath + setted + " " + timeInterval.toString() + days,
                "AutoBackup", 1);
    }

    public static LocalDateTime getNexDateBackup(TimeInterval timeInterval) {
        return LocalDateTime.now()
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static void forceBackupTermination(int requestId) {
        BackupRequestRepository.updateRequestStatusByRequestId(requestId, BackupStatus.TERMINATED);
    }

    public static ConfigurationBackup toggleAutomaticBackup(ConfigurationBackup backup) {
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

            showMessageActivationAutoBackup(timeInterval, backup.getTargetPath(), backup.getDestinationPath());

            updateBackup(backup);

            return backup;
        }

        return null;
    }
}
