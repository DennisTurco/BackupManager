package backupmanager.Repositories;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;

public class Repository {

    private static final Logger logger = LoggerFactory.getLogger(Repository.class);
    private static final String JDBC_PREFIX = "jdbc:sqlite:";
    private static File destFile;
    private static final String INIT_SCRIPT_PATH = ConfigKey.MIGRATION_FILE.getValue();


    public static String getUrlConnection() {
        return JDBC_PREFIX + destFile.getAbsolutePath();
    }

    public static void initDatabaseIfNotExists() throws Exception {
        String userHome = System.getProperty("user.home");
        destFile = new File(userHome + File.separator + "Shard" + File.separator + "data" + File.separator + "BackupManager.db");
        destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            try (InputStream dbStream = Repository.class.getResourceAsStream("/BackupManager.db")) {
                if (dbStream == null) {
                    throw new FileNotFoundException("Error: 'BackupManager.db' not found in resources folder");
                }
                Files.copy(dbStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Database copied to: " + destFile.getAbsolutePath());
                try (Connection conn = DriverManager.getConnection(getUrlConnection())) {
                    runInitScript(conn);
                    logger.info("Database initialized.");
                }
            }
        } else {
            logger.info("Database already exists: " + destFile.getAbsolutePath());
        }
    }

    private static void runInitScript(Connection conn) throws Exception {
        try (InputStream is = Repository.class.getResourceAsStream(INIT_SCRIPT_PATH)) {
            if (is == null) {
                throw new FileNotFoundException("init.sql not found in resources");
            }

            logger.info("Initializing database migration");

            String script = new String(is.readAllBytes());
            String[] commands = script.split(";");

            try (Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                for (String command : commands) {
                    String trimmed = command.trim();
                    if (!trimmed.isEmpty()) {
                        logger.debug("Executing: " + trimmed);
                        stmt.execute(trimmed);
                    }
                }
                conn.commit();
            } catch (Exception e) {
                logger.error("Migration failed", e);
                conn.rollback();
                throw e;
            }
        }
    }
}
