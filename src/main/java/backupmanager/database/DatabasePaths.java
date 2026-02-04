package backupmanager.database;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabasePaths {
    public static Path getDatabasePath() {
        return Paths.get(
            System.getProperty("user.home"),
            "Documents", "Shard", "data", "BackupManager.db"
        );
    }
}
