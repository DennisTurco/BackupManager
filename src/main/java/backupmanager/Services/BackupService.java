package backupmanager.Services;

import java.time.LocalDateTime;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.database.Repositories.BackupConfigurationRepository;

public class BackupService {
    public List<ConfigurationBackup> getAllBackups() {
        return BackupConfigurationRepository.getBackupList();
    }

    public boolean isRunning(String name) {
        return RunningBackupService.getRunningBackupByName(name).isPresent();
    }

    public void deleteBackups(List<String> names) {
        names.forEach(name -> {
            ConfigurationBackup backup = BackupConfigurationRepository.getBackupByName(name);
            if (backup != null) {
                BackupConfigurationRepository.deleteBackup(backup.getId());
            }
        });
    }

    public String getBackupDetails(String name) {
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupByName(name);
        return buildDetails(backup);
    }

    public String buildDetails(ConfigurationBackup backup) {
        String backupNameStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_NAME_DETAIL);
        String initialPathStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.INITIAL_PATH_DETAIL);
        String destinationPathStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.DESTINATION_PATH_DETAIL);
        String lastBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.LAST_BACKUP_DETAIL);
        String nextBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.NEXT_BACKUP_DATE_DETAIL);
        String timeIntervalBackupStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.TIME_INTERVAL_DETAIL);
        String creationDateStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.CREATION_DATE_DETAIL);
        String lastUpdateDateStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.LAST_UPDATE_DATE_DETAIL);
        String backupCountStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.BACKUP_COUNT_DETAIL);
        String notesStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.NOTES_DETAIL);
        String maxBackupsToKeepStr = TranslationCategory.BACKUP_LIST.getTranslation(TranslationKey.MAX_BACKUPS_TO_KEEP_DETAIL);

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
