package backupmanager.database;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void init() throws Exception {
        Path dbPath = DatabasePaths.getDatabasePath();
        Files.createDirectories(dbPath.getParent());

        if (Files.exists(dbPath)) {
            logger.info("Database already exists: {}", dbPath);
        } else {
            logger.info("Creating database: {}", dbPath);
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.setAutoCommit(false);
            runSql(conn, "/db/schema.sql");
            runSql(conn, "/db/seed.sql");
            conn.commit();
        }
    }

    private static void runSql(Connection conn, String resource) throws Exception {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream(resource)) {
            if (is == null) return;

            String sql = new String(is.readAllBytes());
            String[] statements = sql.split(";"); // split per ogni comando

            try (Statement st = conn.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        st.execute(trimmed);
                    }
                }
            }
        }
    }
}
