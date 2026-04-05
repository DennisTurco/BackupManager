package backupmanager.gui.frames.Controllers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Services.BackupService;
import backupmanager.gui.Table.BackupTableDataService;
import backupmanager.gui.forms.CustomForm;
import backupmanager.gui.simple.BackupEntryDialog;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class BackupManagerController {

    private final BackupService backupService;
    private final BackupTableDataService backupTable;

    public BackupManagerController(BackupService backupService, BackupTableDataService backupTable) {
        this.backupService = backupService;
        this.backupTable = backupTable;
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
                        new BackupEntryDialog(backupTable),
                        TCategory.BACKUP_ENTRY.getTranslation(TKey.PAGE_SUBTITLE_CREATE),
                        SimpleModalBorder.OK_CANCEL_OPTION,
                        (controller, action) -> {}
                ),
                option);
    }

    public void showEditModal(CustomForm form, ConfigurationBackup backup) {
        BackupEntryDialog dialog = new BackupEntryDialog(backupTable, backup);

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
                                backupService.updateBackup(editedBackup);
                                form.formRefresh();
                            }
                        }
                ),
                option
        );
    }

    public List<ConfigurationBackup> getAllBackups() {
        return backupService.getAllBackups();
    }

    public String buildDetails(ConfigurationBackup backup) {
        return backupService.buildDetails(backup);
    }
}
