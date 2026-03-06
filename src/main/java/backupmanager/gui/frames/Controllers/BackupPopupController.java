package backupmanager.gui.frames.Controllers;

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
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.gui.Table.BackupTable;
import backupmanager.gui.Table.TableDataManager;
import backupmanager.gui.frames.BackupManagerGUI;
import backupmanager.gui.frames.BackupProgressGUI;

public class BackupPopupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupPopupController.class);

    @Deprecated
    public static void popupItemInterrupt(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            ZippingContext context = ZippingContext.create(backup, null, backupTable, BackupManagerGUI.progressBar, interruptBackupPopupItem, RunBackupPopupItem);
            BackupOperations.interruptBackupProcess(context);
        }
    }

    public static void popupItemInterrupt() {

    }

    @Deprecated
    public static void popupItemRenameBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);
            renameBackup(backups, backup);
        }
    }

    public static void popupItemRenameBackup(List<ConfigurationBackup> backups, ConfigurationBackup backup) {
        renameBackup(backups, backup);
    }

    @Deprecated
    public static void popupItemOpenDestinationPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);
            openFolder(backup.getDestinationPath());
        }
    }

    public static void popupItemOpenDestinationPath(ConfigurationBackup backup) {
        openFolder(backup.getDestinationPath());
    }

    @Deprecated
    public static void popupItemOpenInitialPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            openFolder(backup.getTargetPath());
        }
    }

    public static void popupItemOpenInitialPath(ConfigurationBackup backup) {
        openFolder(backup.getTargetPath());
    }

    @Deprecated
    public static void popupItemAutoBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JCheckBoxMenuItem autoBackupMenuItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            autoBackupMenuItem.setSelected(!backup.isAutomatic());
            BackupHelper.toggleAutomaticBackup(backup);
        }
    }

    public static void popupItemAutoBackup(ConfigurationBackup backup) {
        BackupHelper.toggleAutomaticBackup(backup);
    }

    @Deprecated
    public static void popupItemCopyDestinationPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getDestinationPath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemCopyDestinationPath(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getDestinationPath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    @Deprecated
    public static void popupItemCopyInitialPath(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getTargetPath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemCopyInitialPath(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getTargetPath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    @Deprecated
    public static void popupItemCopyBackupName(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            StringSelection selection = new StringSelection(backup.getName());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public static void popupItemCopyBackupName(ConfigurationBackup backup) {
        StringSelection selection = new StringSelection(backup.getName());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }


    @Deprecated
    public static void popupItemRunBackup(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        if (selectedRow != -1) {
            ConfigurationBackup backup = getBackupByName(backups, selectedRow, backupTable);

            BackupManagerGUI.progressBar = new BackupProgressGUI(backup.getTargetPath(), backup.getDestinationPath());

            ZippingContext context = ZippingContext.create(backup, null, backupTable, BackupManagerGUI.progressBar, interruptBackupPopupItem, RunBackupPopupItem);
            BackupOperations.singleBackup(context, BackupTriggerType.USER);
        }
    }

    public static void popupItemRunBackup(ConfigurationBackup backup, JTable backupTable, JMenuItem interruptBackupPopupItem, JMenuItem RunBackupPopupItem) {
        BackupManagerGUI.progressBar = new BackupProgressGUI(backup.getTargetPath(), backup.getDestinationPath());
        ZippingContext context = ZippingContext.create(backup, null, backupTable, BackupManagerGUI.progressBar, interruptBackupPopupItem, RunBackupPopupItem);
        BackupOperations.singleBackup(context, BackupTriggerType.USER);
    }

    @Deprecated
    public static void popupItemEditBackupName(int selectedRow, BackupTable backupTable, List<ConfigurationBackup> backups, BackupManagerGUI main) {
        if (selectedRow != -1) {
            // get correct backup
            String backupName = (String) backupTable.getValueAt(selectedRow, 0);
            ConfigurationBackup backup = ConfigurationBackup.getBackupByName(new ArrayList<>(backups), backupName);

            logger.info("Edit row : " + selectedRow);
            BackupHelper.openBackupById(backup.getId(), main);
        }
    }

    public static void popupItemEditBackupName(ConfigurationBackup backup) {
        // BackupHelper.openBackupById(backup.getId(), main);
    }

    @Deprecated
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

    public static void popupItemDuplicateBackup(ConfigurationBackup backup) {
        logger.info("Event --> duplicating backup");

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

        // List<ConfigurationBackup> backups = BackupHelper.getBackupList();

        // if (BackupManagerGUI.model != null)
        //     TableDataManager.updateTableWithNewBackupList(backups, BackupHelper.formatter);
    }

    @Deprecated
    public static void popupItemDelete(int selectedRow, BackupTable backupTable) {
        BackupHelper.deleteBackup(selectedRow, backupTable);
    }

    @Deprecated
    private static ConfigurationBackup getBackupByName(int selectedRow, BackupTable backupTable) {
        String backupName = (String) backupTable.getValueAt(selectedRow, 0);
        return BackupConfigurationRepository.getBackupByName(backupName);
    }

    @Deprecated
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
            String backupName = JOptionPane.showInputDialog(null, TCategory.DIALOGS.getTranslation(TKey.BACKUP_NAME_INPUT), oldName);

            // If the user cancels the operation
            if (backupName == null || backupName.trim().isEmpty()) {
                return null;
            }

            Optional<ConfigurationBackup> existingBackup = backups.stream()
                .filter(b -> b.getName().equals(backupName))
                .findFirst();

            if (existingBackup.isPresent()) {
                if (canOverwrite) {
                    int response = JOptionPane.showConfirmDialog(null, TCategory.DIALOGS.getTranslation(TKey.DUPLICATED_BACKUP_NAME_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    backups.remove(existingBackup.get());
                    return backupName;
                }
                } else {
                    logger.warn("Backup name '{}' is already in use", backupName);
                    JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.BACKUP_NAME_ALREADY_USED_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_FOR_FOLDER_NOT_EXISTING), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }
}
