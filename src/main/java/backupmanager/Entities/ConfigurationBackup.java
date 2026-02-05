package backupmanager.Entities;

import java.time.LocalDateTime;
import java.util.List;

import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.database.Repositories.BackupConfigurationRepository;

public class ConfigurationBackup {
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

    private int id;
    private String name;
    private String targetPath;
    private String destinationPath;
    private LocalDateTime lastBackupDate;
    private boolean automatic;
    private LocalDateTime nextBackupDate;
    private TimeInterval timeIntervalBackup;
    private String notes;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private int count;
    private int maxToKeep;

    public ConfigurationBackup() {
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
        maxToKeep = configReader.getConfigValue("MaxCountForSameBackup", 1);
    }

    public ConfigurationBackup(String name, String targetPath, String destinationPath, LocalDateTime lastBackupDate, Boolean automatic, LocalDateTime nextBackupDate, TimeInterval timeIntervalBackup, String notes, LocalDateTime creationDate, LocalDateTime lastUpdateDate, int count, int maxToKeep) {
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

    public ConfigurationBackup(int id, String name, String targetPath, String destinationPath, LocalDateTime lastBackupDate, Boolean automatic, LocalDateTime nextBackupDate, TimeInterval timeIntervalBackup, String notes, LocalDateTime creationDate, LocalDateTime lastUpdateDate, int count, int maxToKeep) {
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

    public ConfigurationBackup(ConfigurationBackup backup) {
        UpdateBackup(backup);
    }

    // make it final to avoid the warning (now this method cannot be overrided by the subclasses)
    public final void UpdateBackup(ConfigurationBackup backupUpdated) {
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

    public static ConfigurationBackup getBackupByName(List<ConfigurationBackup> backups, String name) {
        for (ConfigurationBackup backup : backups) {
            if (backup.getName().equals(name)) {
                return backup;
            }
        }
        return null;
    }

    public static ConfigurationBackup getBackupByName(String name) {
        return BackupConfigurationRepository.getBackupByName(name);
    }

    public static String getCSVHeader() {
        return "BackupName,targetPath,DestinationPath,lastBackupDate,IsAutoBackup,NextDate,Interval (gg.HH:mm),MaxBackupsToKeep";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public LocalDateTime getLastBackupDate() {
        return lastBackupDate;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public LocalDateTime getNextBackupDate() {
        return nextBackupDate;
    }

    public TimeInterval getTimeIntervalBackup() {
        return timeIntervalBackup;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getCount() {
        return count;
    }

    public int getMaxToKeep() {
        return maxToKeep;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public void setLastBackupDate(LocalDateTime lastBackupDate) {
        this.lastBackupDate = lastBackupDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setNextBackupDate(LocalDateTime nextBackupDate) {
        this.nextBackupDate = nextBackupDate;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public void setTimeIntervalBackup(TimeInterval timeIntervalBackup) {
        this.timeIntervalBackup = timeIntervalBackup;
    }

    public void setMaxToKeep(int maxToKeep) {
        this.maxToKeep = maxToKeep;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
