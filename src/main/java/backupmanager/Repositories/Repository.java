package backupmanager.Repositories;

import java.io.*;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repository {

    private static final String JDBC_PREFIX = "jdbc:sqlite:";
    private static File destFile;
    private static final Logger logger = LoggerFactory.getLogger(Repository.class);

    public static String getUrlConnection() {
        return JDBC_PREFIX + destFile.getAbsolutePath();
    }

    public static void initDatabaseIfNotExists() throws IOException {
        try (InputStream dbStream = Repository.class.getResourceAsStream("/BackupManager.db")) {
            if (dbStream == null) {
                throw new FileNotFoundException("Error: file 'database.db' not found on resources folder");
            }

            String userHome = System.getProperty("user.home");
            destFile = new File(userHome + File.separator + "Shard" + File.separator + "data" + File.separator + "BackupManager.db");
            destFile.getParentFile().mkdirs();

            if (!destFile.exists()) {
                Files.copy(dbStream, destFile.toPath());
                logger.info("Database copied to: " + destFile.getAbsolutePath());
            } else {
                logger.info("Database already exists: " + destFile.getAbsolutePath());
            }
        }
    }
}
