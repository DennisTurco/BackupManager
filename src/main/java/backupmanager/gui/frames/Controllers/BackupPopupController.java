package backupmanager.gui.frames.Controllers;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.BackupExecutionContext;
import backupmanager.Entities.BackupUIContext;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Utils.ToastUtils;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.gui.Table.BackupTableDataService;
import backupmanager.gui.frames.BackupProgressGUI;

public class BackupPopupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupPopupController.class);

    public static void popupItemInterrupt() {
        // TODO: add it
    }

    public static void popupItemRenameBackup(JComponent parent, List<ConfigurationBackup> backups, ConfigurationBackup backup) {
        renameBackup(parent, backups, backup);
    }

    public static void popupItemOpenDestinationPath(JComponent parent, ConfigurationBackup backup) {
        openFolder(parent, backup.getDestinationPath());
    }

    public static void popupItemOpenInitialPath(JComponent parent, ConfigurationBackup backup) {
        openFolder(parent, backup.getTargetPath());
    }

    public static void popupItemAutoBackup(JComponent parent, ConfigurationBackup backup) {
        BackupHelper.toggleAutomaticBackup(parent, backup);
    }

    public static void popupItemRunBackup(ConfigurationBackup backup, BackupTableDataService backupTable, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        BackupProgressGUI progressBar = new BackupProgressGUI(backup.getTargetPath(), backup.getDestinationPath());

        ZippingContext context = new ZippingContext(
            BackupExecutionContext.create(backup),
            new BackupUIContext(null, backupTable, progressBar, interruptBackupPopupItem, RunBackupPopupItem)
        );

        BackupOperations.requestSingleBackup(context, BackupTriggerType.USER);
    }

    public static void popupItemCopyDestinationPath(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getDestinationPath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void popupItemCopyInitialPath(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getTargetPath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void popupItemCopyBackupName(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getName());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    public static void popupItemDuplicateBackup(ConfigurationBackup backup) {
        logger.info("Event --> duplicating backup");

        int value = getIncrementalBackupNameValue(backup.getName());

        LocalDateTime dateNow = LocalDateTime.now();
        ConfigurationBackup newBackup = new ConfigurationBackup(
                backup.getName() + "(" + value + ")",
                backup.getTargetPath(),
                backup.getDestinationPath(),
                null,
                backup.isAutomatic(),
                backup.getNextBackupDate(),
                backup.getTimeIntervalBackup(),
                backup.getNotes(),
                dateNow,
                dateNow,
                0,
                backup.getMaxToKeep()
        );

        BackupConfigurationRepository.insertBackup(newBackup);
    }

    private static int getIncrementalBackupNameValue(String backupName) {
        var backups = BackupConfigurationRepository.getBackupList();
        int max = 0;

        Pattern pattern = Pattern.compile(
                Pattern.quote(backupName) + "\\((\\d+)\\)$"
        );

        for (var backup : backups) {
            Matcher matcher = pattern.matcher(backup.getName());
            if (matcher.find()) {
                int value = Integer.parseInt(matcher.group(1));
                max = Math.max(max, value);
            }
        }

        return max + 1;
    }

    private static void renameBackup(JComponent parent, List<ConfigurationBackup> backups, ConfigurationBackup backup) {
        logger.info("Event --> backup renaming");

        String backupName = getBackupNameFromInputDialog(parent, backups, backup.getName(), false);
        if (backupName == null || backupName.isEmpty()) return;

        backup.setName(backupName);
        backup.setLastUpdateDate(LocalDateTime.now());
        BackupHelper.updateBackup(backup);
    }

    private static String getBackupNameFromInputDialog(JComponent parent, List<ConfigurationBackup> backups, String oldName, boolean canOverwrite) {
        while (true) {
            String backupName = JOptionPane.showInputDialog(null, Translations.get(TKey.BACKUP_NAME_INPUT), oldName);

            // If the user cancels the operation
            if (backupName == null || backupName.trim().isEmpty()) {
                return null;
            }

            Optional<ConfigurationBackup> existingBackup = backups.stream()
                .filter(b -> b.getName().equals(backupName))
                .findFirst();

            if (existingBackup.isPresent()) {
                if (canOverwrite) {
                    int response = JOptionPane.showConfirmDialog(null, Translations.get(TKey.DUPLICATED_BACKUP_NAME_MESSAGE), Translations.get(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    backups.remove(existingBackup.get());
                    return backupName;
                }
                } else {
                    logger.warn("Backup name '{}' is already in use", backupName);
                    ToastUtils.showError(parent, Translations.get(TKey.TOAST_BACKUP_NAME_ALREADY_USED));
                }
            } else {
                return backupName;  // Return valid name
            }
        }
    }

    private static void openFolder(JComponent parent, String path) {
        logger.info("Event --> opening folder");

        File folder = new File(path);

        // if the object is a file i want to obtain the folder that contains that file
        if (folder.exists() && folder.isFile()) {
            folder = folder.getParentFile();
        }

        if (folder.exists() && folder.isDirectory()) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(folder);
                } catch (IOException ex) {
                    logger.error("An error occurred: " + ex.getMessage(), ex);
                    ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
                }
            } else {
                logger.warn("Desktop not supported on this operating system");
            }
        } else {
            logger.warn("The folder does not exist or is invalid");
            ToastUtils.showError(parent, Translations.get(TKey.TOAST_FOLDER_NOT_EXISTING));
        }
    }
}
