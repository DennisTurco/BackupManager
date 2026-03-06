package backupmanager.gui.frames.Controllers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Email.EmailSender;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.Configurations;
import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Services.BackupService;
import backupmanager.database.Repositories.UserRepository;
import backupmanager.gui.Dialogs.EntryUserDialog;
import backupmanager.gui.Table.BackupTable;
import backupmanager.gui.forms.CustomForm;
import backupmanager.gui.frames.BackupManagerGUI;
import backupmanager.gui.simple.BackupEntryDialog;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class BackupManagerController {

    private static final Logger logger = LoggerFactory.getLogger(BackupManagerController.class);
    private final BackupService backupService;

    public BackupManagerController(BackupService backupService) {
        this.backupService = backupService;
    }

    @Deprecated
    public void checkForFirstAccess(BackupManagerGUI mainGui) {
        logger.debug("Checking for first access");
        User user = UserRepository.getLastUser();

        if (user == null) {
            setLanguageBasedOnPcLanguage(mainGui);
            createNewUser(mainGui);
        } else {
            logger.info("Current user: " + user.toString());
        }
    }

    public int[] getScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min((int) size.getWidth(), Integer.parseInt(ConfigKey.GUI_WIDTH.getValue()));
        int height = Math.min((int) size.getHeight(), Integer.parseInt(ConfigKey.GUI_HEIGHT.getValue()));
        return new int[]{width, height};
    }

    public List<ConfigurationBackup> researchInTableAndGet(List<ConfigurationBackup> backups, String research) {
        List<ConfigurationBackup> tempBackups = new ArrayList<>();
        research = research.toLowerCase();

        for (ConfigurationBackup backup : backups) {
            if (backup.getName().toLowerCase().contains(research) ||
                    backup.getTargetPath().toLowerCase().contains(research) ||
                    backup.getDestinationPath().toLowerCase().contains(research) ||
                    (backup.getLastBackupDate() != null && backup.getLastBackupDate().toString().toLowerCase().contains(research)) ||
                    (backup.getNextBackupDate() != null && backup.getNextBackupDate().toString().toLowerCase().contains(research)) ||
                    (backup.getTimeIntervalBackup() != null && backup.getTimeIntervalBackup().toString().toLowerCase().contains(research))) {
                tempBackups.add(backup);
            }
        }

        return tempBackups;
    }

    public void showCreateModal(Component parent) {
        Option option = ModalDialog.createOption();
        option.getLayoutOption()
                .setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);

        ModalDialog.showModal(parent,
                new SimpleModalBorder(
                        new BackupEntryDialog(),
                        TCategory.BACKUP_ENTRY.getTranslation(TKey.PAGE_SUBTITLE_CREATE),
                        SimpleModalBorder.OK_CANCEL_OPTION,
                        (controller, action) -> {}
                ),
                option);
    }

    public void showEditModal(CustomForm form, ConfigurationBackup backup) {
        BackupEntryDialog dialog = new BackupEntryDialog(backup);

        Option option = ModalDialog.createOption();
        option.getLayoutOption()
                .setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);

        ModalDialog.showModal(
                form,
                new SimpleModalBorder(
                        dialog,
                        TCategory.BACKUP_ENTRY.getTranslation(TKey.PAGE_SUBTITLE_EDIT),
                        SimpleModalBorder.OK_CANCEL_OPTION,
                        (controller, action) -> {
                            if (action == SimpleModalBorder.OK_OPTION) {
                                ConfigurationBackup editedBackup = dialog.getResult();
                                form.formRefresh();
                            }
                        }
                ),
                option
        );

    }

    public String getBackupDetails(String backupName) {
        return backupService.getBackupDetails(backupName);
    }

    public void deleteBackups(List<String> names) {
        backupService.deleteBackups(names);
    }

    public void deleteBackup(ConfigurationBackup backup) {
        backupService.deleteBackup(backup.getId());
    }

    public boolean isBackupRunning(String name) {
        return backupService.isRunning(name);
    }

    public boolean isAutomaticBackup(List<ConfigurationBackup> backups, String backupName) {
        ConfigurationBackup backup = ConfigurationBackup.getBackupByName(backups, backupName);
        return backup != null && backup.isAutomatic();
    }

    public void openBackup(String name, java.awt.Frame frame) {
        logger.info("Double-click on row: " + name);
        BackupHelper.openBackupByName(name, frame);
    }

    public void handleEnterKeyPressOnTable(java.awt.Frame frame, BackupTable backupTable) {
        int selectedRow = backupTable.getSelectedRow();
        if (selectedRow == -1) return;

        logger.debug("Enter key pressed on row: " + selectedRow);
        BackupHelper.openBackupByName((String) backupTable.getValueAt(selectedRow, 0), frame);

        BackupHelper.openBackupEntryDialog(frame);
    }

    public void handleDeleteKeyPressOnTable(BackupTable backupTable) {
        int[] selectedRows = backupTable.getSelectedRows();
        if (selectedRows.length == 0)
            return;

        logger.debug("Delete key pressed on rows: " + Arrays.toString(selectedRows));

        int response = JOptionPane.showConfirmDialog(null, TCategory.DIALOGS.getTranslation(TKey.CONFIRMATION_DELETION_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.CONFIRMATION_DELETION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response != JOptionPane.YES_OPTION)
            return;

        for (int row : selectedRows)
            BackupHelper.deleteBackup(row, backupTable, false);
    }

    @Deprecated
    private void createNewUser(BackupManagerGUI mainGui) {
        User newUser = null;

        while (newUser == null) {
            newUser = openUserDialogAndObtainTheResult(mainGui);
        }

        UserRepository.insertUser(newUser);

        sendRegistrationEmail(newUser);
    }

    @Deprecated
    private void setLanguageBasedOnPcLanguage(BackupManagerGUI mainGui) {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();

        logger.info("Setting default language to: " + language);

        switch (language) {
            case "en" -> Configurations.setLanguage(LanguagesEnum.ENG);
            case "it" -> Configurations.setLanguage(LanguagesEnum.ITA);
            case "es" -> Configurations.setLanguage(LanguagesEnum.ESP);
            case "de" -> Configurations.setLanguage(LanguagesEnum.DEU);
            case "fr" -> Configurations.setLanguage(LanguagesEnum.FRA);
            default -> Configurations.setLanguage(LanguagesEnum.ENG);
        }

        mainGui.reloadPreferences();
    }

    @Deprecated
    private User openUserDialogAndObtainTheResult(BackupManagerGUI mainGui) {
        EntryUserDialog userDialog = new EntryUserDialog(mainGui, true);
        userDialog.setVisible(true);
        return userDialog.getUser();
    }

    @Deprecated
    private void sendRegistrationEmail(User user) {
        EmailSender.sendUserCreationEmail(user);
        EmailSender.sendConfirmEmailToUser(user);
    }
}
