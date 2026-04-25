package backupmanager.gui.Controllers;

import java.io.File;
import java.time.LocalDateTime;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.BackupExecutionContext;
import backupmanager.Entities.BackupUIContext;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Exceptions.BackupAlreadyRunningException;
import backupmanager.Exceptions.InvalidTimeInterval;
import backupmanager.Helpers.BackupHelper;
import backupmanager.database.Repositories.BackupRequestRepository;
import backupmanager.gui.Table.BackupTableDataService;
import backupmanager.gui.frames.BackupProgressGUI;
import backupmanager.gui.simple.TimePickerDialog;

public class BackupEntryController {
    private static final Logger logger = LoggerFactory.getLogger(BackupEntryController.class);

    private ConfigurationBackup currentBackup;

    public BackupEntryController(ConfigurationBackup currentBackup) {
        this.currentBackup = currentBackup;
    }

    public ConfigurationBackup getBackup(String name, String initialPath, String destinationPath, String notes, boolean autoBackup, int maxBackupsToKeep) {
        LocalDateTime nextDateBackup = null;
        TimeInterval timeInterval = currentBackup != null ? currentBackup.getTimeIntervalBackup() : null;
        if (timeInterval != null){
            nextDateBackup = BackupHelper.getNexDateBackup(timeInterval);
        }

        if (!autoBackup) {
            timeInterval = null;
            nextDateBackup = null;
        }

        if (currentBackup == null) {
            LocalDateTime lastBackup = null;
            LocalDateTime creationDate = LocalDateTime.now();
            LocalDateTime lastUpdateDate = creationDate;
            int backupCount = 0;
            return new ConfigurationBackup(name, initialPath, destinationPath, lastBackup, autoBackup, nextDateBackup, timeInterval, notes, creationDate, lastUpdateDate, backupCount, maxBackupsToKeep);
        } else {
            int id = currentBackup.getId();
            LocalDateTime lastBackup = currentBackup.getLastBackupDate();
            LocalDateTime creationDate = currentBackup.getCreationDate();
            LocalDateTime lastUpdateDate = LocalDateTime.now();
            int backupCount = currentBackup.getCount();
            return new ConfigurationBackup(id, name, initialPath, destinationPath, lastBackup, autoBackup, nextDateBackup, timeInterval, notes, creationDate, lastUpdateDate, backupCount, maxBackupsToKeep);
        }
    }

    public TimeInterval handleTimePickerAction(TimePickerDialog picker, String target, String destination) throws InvalidTimeInterval {
        TimeInterval time = BackupHelper.openTimePicker(picker);
        if (time == null) throw new InvalidTimeInterval();

        LocalDateTime nextDateBackup = BackupHelper.getNexDateBackup(time);

        currentBackup.setTimeIntervalBackup(time);
        currentBackup.setNextBackupDate(nextDateBackup);
        currentBackup.setTargetPath(target);
        currentBackup.setDestinationPath(destination);

        return time;
    }

    public boolean canDisposeAfterOk(String name, String initialPath, String destinationPath, String notes, boolean autoBackup, int maxBackupsToKeep, boolean create) {
        if (name.isBlank() || destinationPath.isBlank() || initialPath.isBlank())
            return false;

        updateCurrentBackup(name, initialPath, destinationPath, notes, autoBackup, maxBackupsToKeep);

        if (create) {
            if (ConfigurationBackup.getBackupByName(currentBackup.getName()) != null) {
                int response = JOptionPane.showConfirmDialog(null, Translations.get(TKey.DUPLICATED_BACKUP_NAME_MESSAGE), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    BackupHelper.deleteBackup(currentBackup.getName());
                } else {
                    return false;
                }
            }
            BackupHelper.newBackup(currentBackup);
        } else {
            BackupHelper.updateBackup(currentBackup);
        }

        return true;
    }

    public void openFileChooser(JTextField filed, boolean allowFiles) {
        String text = BackupOperations.pathSearchWithFileChooser(allowFiles);
        if (text != null) {
            filed.setText(text);
        }
    }

    public boolean toggleAutomaticBackup(String name, String initialPath, String destinationPath, String notes, boolean autoBackup, int maxBackupsToKeep) {
        updateCurrentBackup(name, initialPath, destinationPath,notes, autoBackup, maxBackupsToKeep);

        currentBackup.setAutomatic(!currentBackup.isAutomatic());

        ConfigurationBackup backup = BackupHelper.toggleAutomaticBackup(currentBackup);

        if (backup == null)
            return false;

        currentBackup = backup;

        if (backup.getTimeIntervalBackup() != null) {
            TimeInterval timeInterval = backup.getTimeIntervalBackup();
            currentBackup.setNextBackupDate(BackupHelper.getNexDateBackup(timeInterval));
            return true;
        }

        return false;
    }

    public void handleSingleBackupRequest(BackupTableDataService backupTable, String name, String initialPath, String destinationPath, String notes, boolean autoBackup, int maxBackupsToKeep) throws BackupAlreadyRunningException {
        if (BackupRequestRepository.isAnyBackupRunning()) {
            JOptionPane.showMessageDialog(null,
                Translations.get(TKey.WARNING_BACKUP_ALREADY_IN_PROGRESS_MESSAGE),
                Translations.get(TKey.WARNING_GENERIC_TITLE),
                JOptionPane.WARNING_MESSAGE);
            throw new BackupAlreadyRunningException();
        }

        currentBackup = getBackup(
            name,
            initialPath,
            destinationPath,
            notes,
            autoBackup,
            maxBackupsToKeep
        );

        if (ConfigurationBackup.getBackupByName(currentBackup.getName()) == null) {
            BackupHelper.newBackup(currentBackup);
        }

        singleBackup(initialPath, destinationPath, backupTable);
    }

    private void singleBackup(String target, String destination, BackupTableDataService backupTable) {
        logger.info("Event --> single backup");

        String path1 = target;
        String path2 = destination;

        currentBackup.setTargetPath(path2);

        if (!BackupOperations.checkInputCorrect(currentBackup.getName(), path1, path2, null)) return;

        LocalDateTime dateNow = LocalDateTime.now();

        String date = dateNow.format(BackupHelper.dateForfolderNameFormatter);

        //------------------------------SET ALL THE STRINGS------------------------------
        String name1 = new File(path1).getName();

        name1 = BackupOperations.removeExtension(name1);
        path2 = path2 + "\\" + name1 + " (Backup " + date + ")";

        //------------------------------COPY THE FILE OR DIRECTORY------------------------------
        logger.info("date backup: " + date);

        BackupProgressGUI progressBar = new BackupProgressGUI(path1, path2);
        progressBar.setVisible(true);

        ZippingContext context = new ZippingContext(
            BackupExecutionContext.create(currentBackup),
            new BackupUIContext(null, backupTable, progressBar, null, null)
        );

        BackupOperations.executeBackup(context, BackupTriggerType.USER, path1, path2);

        //if current_file_opened is null it means they are not in a backup but it is a backup not registered
        if (currentBackup.getName() != null && !currentBackup.getName().isEmpty()) {
            currentBackup.setTargetPath(target);
            currentBackup.setDestinationPath(destination);
            currentBackup.setLastBackupDate(LocalDateTime.now());
        }
    }

    public void handleOpenBackupActivationMessage(TimeInterval newtimeInterval, String target, String destination) {
        currentBackup.setTimeIntervalBackup(newtimeInterval);
        BackupHelper.showMessageActivationAutoBackup(newtimeInterval, target, destination);
    }

    public ConfigurationBackup getCurrentBackup() {
        return currentBackup;
    }

    public void setCurrentBackup(ConfigurationBackup currentBackup) {
        this.currentBackup = currentBackup;
    }

    private void updateCurrentBackup(String name, String initialPath, String destinationPath, String notes, boolean autoBackup, int maxBackupsToKeep) {
        if (currentBackup == null) {
            currentBackup = getBackup(
                name,
                initialPath,
                destinationPath,
                notes,
                autoBackup,
                maxBackupsToKeep
            );
            return;
        }

        currentBackup.setName(name);
        currentBackup.setTargetPath(initialPath);
        currentBackup.setDestinationPath(destinationPath);
        currentBackup.setNotes(notes);
        currentBackup.setAutomatic(autoBackup);
        currentBackup.setMaxToKeep(maxBackupsToKeep);
        currentBackup.setLastUpdateDate(LocalDateTime.now());
    }
}
