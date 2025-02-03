package backupmanager.Entities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JSONBackup;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Managers.ExceptionManager;

public class Backup {
    private static final Logger logger = LoggerFactory.getLogger(Backup.class);
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    private String _backupName;
    private String _initialPath;
    private String _destinationPath;
    private LocalDateTime _lastBackup;
    private boolean _autoBackup;
    private LocalDateTime _nextDateBackup;
    private TimeInterval _timeIntervalBackup;
    private String _notes;
    private LocalDateTime _creationDate;
    private LocalDateTime _lastUpdateDate;
    private int _backupCount;
    private int _maxBackupsToKeep;
    
    public Backup() {
        _backupName = "";
        _initialPath = "";
        _destinationPath = "";
        _lastBackup = null;
        _autoBackup = false;
        _nextDateBackup = null;
        _timeIntervalBackup = null;
        _notes = "";
        _creationDate = null;
        _lastUpdateDate = null;
        _backupCount = 0;
        _maxBackupsToKeep = configReader.getMaxCountForSameBackup();
    }
    
    public Backup(String backupName, String initialPath, String destinationPath, LocalDateTime lastBackup, Boolean autoBackup, LocalDateTime nextDateBackup, TimeInterval timeIntervalBackup, String notes, LocalDateTime creationDate, LocalDateTime lastUpdateDate, int backupCount, int maxBackupsToKeep) {
        this._backupName = backupName;
        this._initialPath = initialPath;
        this._destinationPath = destinationPath;
        this._lastBackup = lastBackup;
        this._autoBackup = autoBackup;
        this._nextDateBackup = nextDateBackup;
        this._timeIntervalBackup = timeIntervalBackup;
        this._notes = notes;
        this._creationDate = creationDate;
        this._lastUpdateDate = lastUpdateDate;
        this._backupCount = backupCount;
        this._maxBackupsToKeep = maxBackupsToKeep;
    }

    public Backup(Backup backup) {
        UpdateBackup(backup);
    }
    
    // make it final to avoid the warning (now this method cannot be overrided by the subclasses)
    public final void UpdateBackup(Backup backupUpdated) {
        this._backupName = backupUpdated.getBackupName();
        this._initialPath = backupUpdated.getInitialPath();
        this._destinationPath = backupUpdated.getDestinationPath();
        this._lastBackup = backupUpdated.getLastBackup();
        this._autoBackup = backupUpdated.isAutoBackup();
        this._nextDateBackup = backupUpdated.getNextDateBackup();
        this._timeIntervalBackup = backupUpdated.getTimeIntervalBackup();
        this._notes = backupUpdated.getNotes();
        this._creationDate = backupUpdated.getCreationDate();
        this._lastUpdateDate = backupUpdated.getLastUpdateDate();
        this._backupCount = backupUpdated.getBackupCount();
        this._maxBackupsToKeep = backupUpdated.getMaxBackupsToKeep();
    }
    
    @Override
    public String toString() {
        return String.format("[Name: %s, InitialPath: %s, DestinationPath: %s, LastBackup: %s, IsAutoBackup: %s, NextDate: %s, Interval: %s, MaxBackupsToKeep: %d]",
            _backupName,
            _initialPath,
            _destinationPath,
            _lastBackup,
            _autoBackup,
            _nextDateBackup,
            _timeIntervalBackup != null ? _timeIntervalBackup.toString() : "",
            _maxBackupsToKeep
        );
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%d",
            _backupName,
            _initialPath,
            _destinationPath,
            _lastBackup != null ? _lastBackup.toString() : "",
            _autoBackup,
            _nextDateBackup != null ? _nextDateBackup.toString() : "",
            _timeIntervalBackup != null ? _timeIntervalBackup.toString() : "",
            _maxBackupsToKeep
        );
    }

    public static Backup getBackupByName(List<Backup> backups, String backupName) {
        for (Backup backup : backups) {
            if (backup.getBackupName().equals(backupName)) {
                return backup;
            }
        }
        return null;
    }

    public static Backup getBackupByName(String backupName) {
        List<Backup> backups;
        try {
            backups = new JSONBackup().readBackupListFromJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile());
            for (Backup backup : backups) {
                if (backup.getBackupName().equals(backupName)) {
                    return backup;
                }
            }
        } catch (IOException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }

        return null;
    }

    public static String getCSVHeader() {
        return "BackupName,InitialPath,DestinationPath,LastBackup,IsAutoBackup,NextDate,Interval (gg.HH:mm),MaxBackupsToKeep";
    }

    public String getBackupName() {
        return _backupName;
    }
    public String getInitialPath() {
        return _initialPath;
    }
    public String getDestinationPath() {
        return _destinationPath;
    }
    public LocalDateTime getLastBackup() {
        return _lastBackup;
    }
    public boolean isAutoBackup() {
        return _autoBackup;
    }
    public LocalDateTime getNextDateBackup() {
        return _nextDateBackup;
    }
    public TimeInterval getTimeIntervalBackup() {
        return _timeIntervalBackup;
    }
    public String getNotes() {
        return _notes;
    }
    public LocalDateTime getCreationDate() {
        return _creationDate;
    }
    public LocalDateTime getLastUpdateDate() {
        return _lastUpdateDate;
    }
    public int getBackupCount() {
        return _backupCount;
    }
    public int getMaxBackupsToKeep() {
        return _maxBackupsToKeep;
    }
    
    public void setBackupName(String backupName) {
        this._backupName = backupName;
    }
    public void setInitialPath(String initialPath) {
        this._initialPath = initialPath;
    }
    public void setDestinationPath(String destinationPath) {
        this._destinationPath = destinationPath;
    }
    public void setLastBackup(LocalDateTime lastBackup) {
        this._lastBackup = lastBackup;
    }
    public void setAutoBackup(Boolean autoBackup) {
        this._autoBackup = autoBackup;
    }
    public void setNextDateBackup(LocalDateTime nextDateBackup) {
        this._nextDateBackup = nextDateBackup;
    }
    public void setTimeIntervalBackup(TimeInterval timeIntervalBackup) {
        this._timeIntervalBackup = timeIntervalBackup;
    }
    public void setNotes(String notes) {
        this._notes = notes;
    }
    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this._lastUpdateDate = lastUpdateDate;
    }
    public void setBackupCount(int backupCount) {
        this._backupCount = backupCount;
    }
    public void setMaxBackupsToKeep(int maxBackupsToKeep) {
        this._maxBackupsToKeep = maxBackupsToKeep;
    }
}