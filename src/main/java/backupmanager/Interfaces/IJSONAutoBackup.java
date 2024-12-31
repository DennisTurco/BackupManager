package backupmanager.Interfaces;

import java.io.IOException;
import java.util.List;

import backupmanager.Entities.Backup;

public interface IJSONAutoBackup {
    public List<Backup> readBackupListFromJSON(String directoryPath, String filename) throws IOException;
    public void updateBackupListJSON(String directoryPath, String filename, List<Backup> backups);
    public void updateSingleBackupInJSON(String directoryPath, String filename, Backup updatedBackup);
}
