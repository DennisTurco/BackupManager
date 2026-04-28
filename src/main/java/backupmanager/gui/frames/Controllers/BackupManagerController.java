package backupmanager.gui.frames.Controllers;

import java.util.ArrayList;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Services.BackupService;
import backupmanager.Utils.ToastUtils;
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

    public void showCreateModal(CustomForm form) {
        Option option = ModalDialog.createOption();
        option.getLayoutOption()
                .setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);

        BackupEntryDialog dialog = new BackupEntryDialog(backupTable);

        ModalDialog.showModal(
                form,
                new SimpleModalBorder(
                        dialog,
                        Translations.get(TKey.PAGE_SUBTITLE_CREATE),
                        SimpleModalBorder.OK_CANCEL_OPTION,
                        (controller, action) -> {
                            if (action == SimpleModalBorder.OK_OPTION) {
                                if (!dialog.canDispose()) {
                                    return;
                                }

                                ConfigurationBackup backup = dialog.getResult();
                                backupService.updateBackup(backup);

                                ToastUtils.showSuccess(form, Translations.get(TKey.TOAST_BACKUP_CREATED));

                                form.formRefresh();
                                controller.close();
                            }
                        }
                ),
                option
        );
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
                        Translations.get(TKey.PAGE_SUBTITLE_EDIT),
                        SimpleModalBorder.OK_CANCEL_OPTION,
                        (controller, action) -> {
                            if (action == SimpleModalBorder.OK_OPTION) {
                                if (!dialog.canDispose()) {
                                    return;
                                }

                                ConfigurationBackup editedBackup = dialog.getResult();
                                backupService.updateBackup(editedBackup);

                                ToastUtils.showSuccess(form, Translations.get(TKey.TOAST_BACKUP_EDITED));

                                form.formRefresh();
                                controller.close();
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
