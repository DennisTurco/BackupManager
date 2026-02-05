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

        boolean isNewDatabase = !Files.exists(dbPath);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Statement st = conn.createStatement()) {

            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA temp_store=MEMORY;");
            st.execute("PRAGMA foreign_keys=ON;");

            if (isNewDatabase) {
                logger.info("Creating database: {}", dbPath);

                conn.setAutoCommit(false);

                runSql(conn, "/db/schema.sql");
                runSql(conn, "/db/seed.sql");

                conn.commit();
            } else {
                logger.info("Database already exists: {}", dbPath);
            }
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
