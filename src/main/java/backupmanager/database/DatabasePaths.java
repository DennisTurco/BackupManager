package backupmanager.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DatabasePaths {
    public static Path getProductionDatabasePath() {
        return getDocumentsDirectory().resolve(Paths.get("Shard", "data", "BackupManager.db"));
    }

    public static Path getTestDatabasePath() {
        return Paths.get("data", "BackupManager.db");
    }

    // On Linux the Documents folder is localized (e.g. ~/Documenti) and declared in
    // ~/.config/user-dirs.dirs. A database already living in ~/Documents keeps being used.
    private static Path getDocumentsDirectory() {
        Path home = Paths.get(System.getProperty("user.home"));
        Path defaultDocuments = home.resolve("Documents");

        if (!System.getProperty("os.name", "").toLowerCase().startsWith("linux")
                || Files.exists(defaultDocuments.resolve(Paths.get("Shard", "data", "BackupManager.db"))))
            return defaultDocuments;

        Path xdgDocuments = readXdgDocumentsDirectory(home);
        return xdgDocuments != null ? xdgDocuments : defaultDocuments;
    }

    private static Path readXdgDocumentsDirectory(Path home) {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        Path userDirs = (configHome != null && !configHome.isBlank() ? Paths.get(configHome) : home.resolve(".config"))
            .resolve("user-dirs.dirs");
        // Running with a custom user.home (first-launch sandbox): the real user's dirs don't apply
        if (!userDirs.startsWith(home) || !Files.isRegularFile(userDirs)) return null;

        try {
            List<String> lines = Files.readAllLines(userDirs);
            for (String line : lines) {
                line = line.trim();
                if (!line.startsWith("XDG_DOCUMENTS_DIR=")) continue;
                String value = line.substring("XDG_DOCUMENTS_DIR=".length()).replace("\"", "");
                value = value.replace("$HOME", home.toString());
                Path dir = Paths.get(value);
                // "$HOME/" alone means the feature is disabled: fall back to ~/Documents
                return dir.isAbsolute() && !dir.equals(home) ? dir : null;
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
        return null;
    }
}
