package backupmanager.Helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Dialogs.BackupEntryDialog;
import backupmanager.Dialogs.TimePicker;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;
import backupmanager.Table.TableDataManager;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackupHelper {

    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    public static final DateTimeFormatter dateForfolderNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH-mm-ss");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static void openBackupById(int id, BackupManagerGUI main) {
        logger.info("Event --> opening backup");

        ConfigurationBackup backup = BackupConfigurationRepository.getBackupById(id);

        BackupEntryDialog dialog = new BackupEntryDialog(main, false, backup);
        dialog.setVisible(true);
    }

    public static void newBackup(BackupProgressGUI progressBar, BackupManagerGUI main) {
        logger.info("Event --> new backup");

        BackupEntryDialog dialog = new BackupEntryDialog(main, false);
        dialog.setVisible(true);
    }

    public static void newBackup(ConfigurationBackup backup) {
        BackupConfigurationRepository.insertBackup(backup);

        updateBackupTable();
    }

    public static void deleteBackup(int selectedRow, BackupTable backupTable, boolean isConfermationRequired) {
        logger.info("Event --> deleting backup");

        if (isConfermationRequired) {
            int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String backupName = (String) backupTable.getValueAt(selectedRow, 0);
        removeBackup(backupName);
    }

    public static void updateBackup(ConfigurationBackup updatedBackup) {
        if (updatedBackup == null) {
            throw new IllegalArgumentException("Backup is null!");
        }

        if (updatedBackup.getId() != 0) {
            logger.info("Updating backup: " + updatedBackup.getName());
            BackupConfigurationRepository.updateBackup(updatedBackup);
        }

        updateBackupTable();
    }

    public static List<ConfigurationBackup> getBackupList() {
        List<ConfigurationBackup> backups = BackupConfigurationRepository.getBackupList();
        BackupManagerGUI.backups = backups; // i have to keep update also the backup list in the main panel
        return backups;
    }

    public static TimeInterval openTimePicker(java.awt.Dialog parent, TimeInterval time) {
        TimePicker picker = new TimePicker(parent, time, true);
        picker.setVisible(true);
        return picker.getTimeInterval();
    }

    public static void showMessageActivationAutoBackup(TimeInterval timeInterval, String startPath, String destinationPath) {
        String from = TranslationCategory.GENERAL.getTranslation(TranslationKey.FROM);
        String to = TranslationCategory.GENERAL.getTranslation(TranslationKey.TO);
        String activated = TranslationCategory.DIALOGS.getTranslation(TranslationKey.AUTO_BACKUP_ACTIVATED_MESSAGE);
        String setted = TranslationCategory.DIALOGS.getTranslation(TranslationKey.SETTED_EVERY_MESSAGE);
        String days = TranslationCategory.DIALOGS.getTranslation(TranslationKey.DAYS_MESSAGE);

        JOptionPane.showMessageDialog(null,
                activated + "\n\t" + from + ": " + startPath + "\n\t" + to + ": "
                + destinationPath + setted + " " + timeInterval.toString() + days,
                "AutoBackup", 1);
    }

    public static void openBackupByName(String backupName, BackupManagerGUI main) {
        logger.info("Event --> opening backup");

        ConfigurationBackup backup = BackupConfigurationRepository.getBackupByName(backupName);

        BackupEntryDialog dialog = new BackupEntryDialog(main, false, backup);
        dialog.setVisible(true);
    }

    public static void openBackupEntryDialog(BackupManagerGUI main) {
        BackupEntryDialog dialog = new BackupEntryDialog(main, false);
        dialog.setVisible(true);
    }

    public static LocalDateTime getNexDateBackup(TimeInterval timeInterval) {
        return LocalDateTime.now()
            .plusDays(timeInterval.days())
            .plusHours(timeInterval.hours())
            .plusMinutes(timeInterval.minutes());
    }

    public static void deleteBackup(int selectedRow, BackupTable backupTable) {
        logger.info("Event --> deleting backup");

        if (selectedRow != -1) {
            int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_MESSAGE_BEFORE_DELETE_BACKUP), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                String backupName = (String) backupTable.getValueAt(selectedRow, 0);
                BackupHelper.removeBackup(backupName);
            }
        }
    }

    public static void forceBackupTermination(int requestId) {
        BackupRequestRepository.updateRequestStatusByRequestId(requestId, BackupStatus.TERMINATED);
        updateBackupTable();
    }

    public static void removeBackup(String backupName) {
        ConfigurationBackup backup = ConfigurationBackup.getBackupByName(backupName);
        removeBackup(backup);
    }

    private static void removeBackup(ConfigurationBackup backup) {
        logger.info("Event --> removing backup" + backup.getName());
        BackupConfigurationRepository.deleteBackup(backup.getId());
        updateBackupTable();
    }

    private static void updateBackupTable() {
        if (BackupManagerGUI.model != null)
            TableDataManager.updateTableWithNewBackupList(getBackupList(), formatter);
    }

    public static ConfigurationBackup toggleAutomaticBackup(ConfigurationBackup backup) {
        logger.info("Event --> automatic backup");

        if (backup.isAutomatic()) {
            int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_MESSAGE_CANCEL_AUTO_BACKUP), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
            TimeInterval timeInterval = openTimePicker(null, null);
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
