package backupmanager.Entities;

import java.time.LocalDateTime;
import java.util.List;
import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Repositories.BackupConfigurationRepository;
import lombok.Getter;
import lombok.Setter;

public class Backup {
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

    @Getter @Setter private int id;
    @Getter @Setter private String name;
    @Getter @Setter private String targetPath;
    @Getter @Setter private String destinationPath;
    @Getter @Setter private LocalDateTime lastBackupDate;
    @Getter @Setter private boolean automatic;
    @Getter @Setter private LocalDateTime nextBackupDate;
    @Getter @Setter private TimeInterval timeIntervalBackup;
    @Getter @Setter private String notes;
    @Getter @Setter private LocalDateTime creationDate;
    @Getter @Setter private LocalDateTime lastUpdateDate;
    @Getter @Setter private int count;
    @Getter @Setter private int maxToKeep;

    public Backup() {
        id = 0;
        name = "";
        targetPath = "";
        destinationPath = "";
        lastBackupDate = null;
        automatic = false;
        nextBackupDate = null;
        timeIntervalBackup = null;
        notes = "";
        creationDate = null;
        lastUpdateDate = null;
        count = 0;
        maxToKeep = configReader.getMaxCountForSameBackup();
    }

    public Backup(String name, String targetPath, String destinationPath, LocalDateTime lastBackupDate, Boolean automatic, LocalDateTime nextBackupDate, TimeInterval timeIntervalBackup, String notes, LocalDateTime creationDate, LocalDateTime lastUpdateDate, int count, int maxToKeep) {
        this.name = name;
        this.targetPath = targetPath;
        this.destinationPath = destinationPath;
        this.lastBackupDate = lastBackupDate;
        this.automatic = automatic;
        this.nextBackupDate = nextBackupDate;
        this.timeIntervalBackup = timeIntervalBackup;
        this.notes = notes;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.count = count;
        this.maxToKeep = maxToKeep;
    }

    public Backup(int id, String name, String targetPath, String destinationPath, LocalDateTime lastBackupDate, Boolean automatic, LocalDateTime nextBackupDate, TimeInterval timeIntervalBackup, String notes, LocalDateTime creationDate, LocalDateTime lastUpdateDate, int count, int maxToKeep) {
        this.id = id;
        this.name = name;
        this.targetPath = targetPath;
        this.destinationPath = destinationPath;
        this.lastBackupDate = lastBackupDate;
        this.automatic = automatic;
        this.nextBackupDate = nextBackupDate;
        this.timeIntervalBackup = timeIntervalBackup;
        this.notes = notes;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.count = count;
        this.maxToKeep = maxToKeep;
    }

    public Backup(Backup backup) {
        UpdateBackup(backup);
    }

    // make it final to avoid the warning (now this method cannot be overrided by the subclasses)
    public final void UpdateBackup(Backup backupUpdated) {
        this.id = backupUpdated.getId();
        this.name = backupUpdated.getName();
        this.targetPath = backupUpdated.getTargetPath();
        this.destinationPath = backupUpdated.getDestinationPath();
        this.lastBackupDate = backupUpdated.getLastBackupDate();
        this.automatic = backupUpdated.isAutomatic();
        this.nextBackupDate = backupUpdated.getNextBackupDate();
        this.timeIntervalBackup = backupUpdated.getTimeIntervalBackup();
        this.notes = backupUpdated.getNotes();
        this.creationDate = backupUpdated.getCreationDate();
        this.lastUpdateDate = backupUpdated.getLastUpdateDate();
        this.count = backupUpdated.getCount();
        this.maxToKeep = backupUpdated.getMaxToKeep();
    }

    @Override
    public String toString() {
        return String.format("[Id: %d, Name: %s, targetPath: %s, DestinationPath: %s, lastBackupDate: %s, IsAutoBackup: %s, NextDate: %s, Interval: %s, MaxBackupsToKeep: %d]",
            id,
            name,
            targetPath,
            destinationPath,
            lastBackupDate,
            automatic,
            nextBackupDate,
            timeIntervalBackup != null ? timeIntervalBackup.toString() : "",
            maxToKeep
        );
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%d",
            name,
            targetPath,
            destinationPath,
            lastBackupDate != null ? lastBackupDate.toString() : "",
            automatic,
            nextBackupDate != null ? nextBackupDate.toString() : "",
            timeIntervalBackup != null ? timeIntervalBackup.toString() : "",
            maxToKeep
        );
    }

    public static Backup getBackupByName(List<Backup> backups, String name) {
        for (Backup backup : backups) {
            if (backup.getName().equals(name)) {
                return backup;
            }
        }
        return null;
    }

    public static Backup getBackupByName(String name) {
        return BackupConfigurationRepository.getBackupByName(name);
    }

    public static String getCSVHeader() {
        return "BackupName,targetPath,DestinationPath,lastBackupDate,IsAutoBackup,NextDate,Interval (gg.HH:mm),MaxBackupsToKeep";
    }
}