package backupmanager.GUI.Controllers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Dialogs.EntryUserDialog;
import backupmanager.Email.EmailSender;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.Confingurations;
import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import static backupmanager.GUI.BackupManagerGUI.backups;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.Services.BackupService;
import backupmanager.Table.BackupTable;
import backupmanager.Table.TableDataManager;
import backupmanager.database.Repositories.UserRepository;

public class BackupManagerController {

    private static final Logger logger = LoggerFactory.getLogger(BackupManagerGUI.class);
    private final BackupService backupService;

    public BackupManagerController(BackupService backupService) {
        this.backupService = backupService;
    }

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

    public void researchInTable(String research) {
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

        TableDataManager.updateTableWithNewBackupList(tempBackups, formatter);
    }

    public String getBackupDetails(String backupName) {
        return backupService.getBackupDetails(backupName);
    }

    public void deleteBackups(List<String> names) {
        backupService.deleteBackups(names);
    }

    public boolean isBackupRunning(String name) {
        return backupService.isRunning(name);
    }

    public boolean isAutomaticBackup(String backupName) {
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

        int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response != JOptionPane.YES_OPTION)
            return;

        for (int row : selectedRows)
            BackupHelper.deleteBackup(row, backupTable, false);
    }

    private void createNewUser(BackupManagerGUI mainGui) {
        User newUser = null;

        while (newUser == null) {
            newUser = openUserDialogAndObtainTheResult(mainGui);
        }

        UserRepository.insertUser(newUser);

        sendRegistrationEmail(newUser);
    }

    private void setLanguageBasedOnPcLanguage(BackupManagerGUI mainGui) {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();

        logger.info("Setting default language to: " + language);

        switch (language) {
            case "en" -> Confingurations.setLanguage(LanguagesEnum.ENG);
            case "it" -> Confingurations.setLanguage(LanguagesEnum.ITA);
            case "es" -> Confingurations.setLanguage(LanguagesEnum.ESP);
            case "de" -> Confingurations.setLanguage(LanguagesEnum.DEU);
            case "fr" -> Confingurations.setLanguage(LanguagesEnum.FRA);
            default -> Confingurations.setLanguage(LanguagesEnum.ENG);
        }

        mainGui.reloadPreferences();
    }

    private User openUserDialogAndObtainTheResult(BackupManagerGUI mainGui) {
        EntryUserDialog userDialog = new EntryUserDialog(mainGui, true);
        userDialog.setVisible(true);
        return userDialog.getUser();
    }

    private void sendRegistrationEmail(User user) {
        EmailSender.sendUserCreationEmail(user);
        EmailSender.sendConfirmEmailToUser(user);
    }
}
