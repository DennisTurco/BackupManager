package backupmanager.Services;

import java.time.LocalDateTime;
import java.util.List;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.database.Repositories.BackupConfigurationRepository;

public class BackupService {
    public List<ConfigurationBackup> getAllBackups() {
        return BackupConfigurationRepository.getBackupList();
    }

    public void updateBackup(ConfigurationBackup backup) {
        BackupConfigurationRepository.updateBackup(backup);
    }

    public String buildDetails(ConfigurationBackup backup) {
        String backupNameStr = Translations.get(TKey.BACKUP_NAME_DETAIL);
        String initialPathStr = Translations.get(TKey.INITIAL_PATH_DETAIL);
        String destinationPathStr = Translations.get(TKey.DESTINATION_PATH_DETAIL);
        String lastBackupStr = Translations.get(TKey.LAST_BACKUP_DETAIL);
        String nextBackupStr = Translations.get(TKey.NEXT_BACKUP_DATE_DETAIL);
        String timeIntervalBackupStr = Translations.get(TKey.TIME_INTERVAL_DETAIL);
        String creationDateStr = Translations.get(TKey.CREATION_DATE_DETAIL);
        String lastUpdateDateStr = Translations.get(TKey.LAST_UPDATE_DATE_DETAIL);
        String backupCountStr = Translations.get(TKey.BACKUP_COUNT_DETAIL);
        String notesStr = Translations.get(TKey.NOTES_DETAIL);
        String maxBackupsToKeepStr = Translations.get(TKey.MAX_BACKUPS_TO_KEEP_DETAIL);

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
