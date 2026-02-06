package backupmanager.GUI.Controllers;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggeredEnum;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Table.BackupTable;
import backupmanager.Table.TableDataManager;
import backupmanager.database.Repositories.BackupConfigurationRepository;

public class BackupPopupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupPopupController.class);

    public static void popupItemInterrupt(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            ZippingContext context = ZippingContext.create(backup, null, backupTable, BackupManagerGUI.progressBar, interruptBackupPopupItem, RunBackupPopupItem);
            BackupOperations.interruptBackupProcess(context);
        }
    }

    public static void popupItemRenameBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);
            renameBackup(backups, backup);
        }
    }

    public static void popupItemOpenDestinationPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);
            openFolder(backup.getDestinationPath());
        }
    }

    public static void popupItemOpenInitialPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            openFolder(backup.getTargetPath());
        }
    }

    public static void popupItemAutoBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JCheckBoxMenuItem autoBackupMenuItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            autoBackupMenuItem.setSelected(!backup.isAutomatic());
            BackupHelper.toggleAutomaticBackup(backup);
        }
    }

    public static void popupItemCopyDestinationPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getDestinationPath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemCopyInitialPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getTargetPath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemCopyBackupName(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getName());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemRunBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            BackupManagerGUI.progressBar = new BackupProgressGUI(backup.getTargetPath(), backup.getDestinationPath());

            ZippingContext context = ZippingContext.create(backup, null, backupTable, BackupManagerGUI.progressBar, interruptBackupPopupItem, RunBackupPopupItem);
            BackupOperations.SingleBackup(context, BackupTriggeredEnum.USER);
        }
    }

    public static void popupItemEditBackupName(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, BackupManagerGUI main) {
        if (selectedRow != -1) {
            // get correct backup
            String backupName = (String) backupTable.getValueAt(selectedRow, 0);
            ConfigurationBackup backup = ConfigurationBackup.getBackupByName(new ArrayList<>(backups), backupName);

            logger.info("Edit row : " + selectedRow);
            BackupHelper.openBackupById(backup.getId(), main);
        }
    }

    public static void popupItemDuplicateBackup(int selectedRow, BackupTable backupTable) {
        logger.info("Event --> duplicating backup");

        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(selectedRow, backupTable);

            LocalDateTime dateNow = LocalDateTime.now();
            ConfigurationBackup newBackup = new ConfigurationBackup(
                    backup.getName() + "_copy",
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

            List<ConfigurationBackup> backups = BackupHelper.getBackupList();

            if (BackupManagerGUI.model != null)
                TableDataManager.updateTableWithNewBackupList(backups, BackupHelper.formatter);
            }
    }

    public static void popupItemDelete(int selectedRow, BackupTable backupTable) {
        BackupHelper.deleteBackup(selectedRow, backupTable);
    }

    private static ConfigurationBackup getBackupByName(int selectedRow, BackupTable backupTable) {
        String backupName = (String) backupTable.getValueAt(selectedRow, 0);
        return BackupConfigurationRepository.getBackupByName(backupName);
    }

    private static ConfigurationBackup getBackupByName(List<ConfigurationBackup> backups, int selectedRow, BackupTable backupTable) {
        String backupName = (String) backupTable.getValueAt(selectedRow, 0);
        return ConfigurationBackup.getBackupByName(backups, backupName);
    }

    private static void renameBackup(List<ConfigurationBackup> backups, ConfigurationBackup backup) {
        logger.info("Event --> backup renaming");

        String backupName = getBackupNameFromInputDialog(backups, backup.getName(), false);
        if (backupName == null || backupName.isEmpty()) return;

        backup.setName(backupName);
        backup.setLastUpdateDate(LocalDateTime.now());
        BackupHelper.updateBackup(backup);
    }

    private static String getBackupNameFromInputDialog(List<ConfigurationBackup> backups, String oldName, boolean canOverwrite) {
        while (true) {
            String backupName = JOptionPane.showInputDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_NAME_INPUT), oldName);

            // If the user cancels the operation
            if (backupName == null || backupName.trim().isEmpty()) {
                return null;
            }

            Optional<ConfigurationBackup> existingBackup = backups.stream()
                .filter(b -> b.getName().equals(backupName))
                .findFirst();

            if (existingBackup.isPresent()) {
                if (canOverwrite) {
                    int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.DUPLICATED_BACKUP_NAME_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    backups.remove(existingBackup.get());
                    return backupName;
                }
                } else {
                    logger.warn("Backup name '{}' is already in use", backupName);
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.BACKUP_NAME_ALREADY_USED_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                return backupName;  // Return valid name
            }
        }
    }

    private static void openFolder(String path) {
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
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_FOLDER_NOT_EXISTING), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }
}
