package backupmanager.Services;

import java.time.LocalDateTime;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.database.Repositories.BackupConfigurationRepository;

public class BackupService {
    public List<ConfigurationBackup> getAllBackups() {
        return BackupConfigurationRepository.getBackupList();
    }

    public boolean isRunning(String name) {
        return RunningBackupService.getRunningBackupByName(name).isPresent();
    }

    public void deleteBackup(int id) {
        BackupConfigurationRepository.deleteBackup(id);
    }

    public void deleteBackups(List<String> names) {
        names.forEach(name -> {
            ConfigurationBackup backup = getBackupByName(name);
            if (backup != null) {
                BackupConfigurationRepository.deleteBackup(backup.getId());
            }
        });
    }

    public String getBackupDetails(String name) {
        ConfigurationBackup backup = getBackupByName(name);
        return buildDetails(backup);
    }

    public ConfigurationBackup getBackupByName(String name) {
        return BackupConfigurationRepository.getBackupByName(name);
    }

    public String buildDetails(ConfigurationBackup backup) {
        String backupNameStr = TCategory.BACKUP_LIST.getTranslation(TKey.BACKUP_NAME_DETAIL);
        String initialPathStr = TCategory.BACKUP_LIST.getTranslation(TKey.INITIAL_PATH_DETAIL);
        String destinationPathStr = TCategory.BACKUP_LIST.getTranslation(TKey.DESTINATION_PATH_DETAIL);
        String lastBackupStr = TCategory.BACKUP_LIST.getTranslation(TKey.LAST_BACKUP_DETAIL);
        String nextBackupStr = TCategory.BACKUP_LIST.getTranslation(TKey.NEXT_BACKUP_DATE_DETAIL);
        String timeIntervalBackupStr = TCategory.BACKUP_LIST.getTranslation(TKey.TIME_INTERVAL_DETAIL);
        String creationDateStr = TCategory.BACKUP_LIST.getTranslation(TKey.CREATION_DATE_DETAIL);
        String lastUpdateDateStr = TCategory.BACKUP_LIST.getTranslation(TKey.LAST_UPDATE_DATE_DETAIL);
        String backupCountStr = TCategory.BACKUP_LIST.getTranslation(TKey.BACKUP_COUNT_DETAIL);
        String notesStr = TCategory.BACKUP_LIST.getTranslation(TKey.NOTES_DETAIL);
        String maxBackupsToKeepStr = TCategory.BACKUP_LIST.getTranslation(TKey.MAX_BACKUPS_TO_KEEP_DETAIL);

        return """
            <html>
            <div style='font-family:sans-serif; font-size:10px; padding:2px'>

            <b>%s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.
            <b> %s:</b> %s.

            </div>
            </html>
            """.formatted(
                backupNameStr, backup.getName(),
                initialPathStr, backup.getTargetPath(),
                destinationPathStr, backup.getDestinationPath(),
                lastBackupStr, formatDate(backup.getLastBackupDate()),
                nextBackupStr, formatDate(backup.getNextBackupDate()),
                timeIntervalBackupStr, optionalString(backup.getTimeIntervalBackup()),
                creationDateStr, formatDate(backup.getCreationDate()),
                lastUpdateDateStr, formatDate(backup.getLastUpdateDate()),
                backupCountStr, backup.getCount(),
                maxBackupsToKeepStr, backup.getMaxToKeep(),
                notesStr, backup.getNotes()
            );
    }

    private String formatDate(LocalDateTime date) {
        return date != null ? date.format(formatter) : "_";
    }

    private String optionalString(Object value) {
        return value != null ? value.toString() : "_";
    }
}
