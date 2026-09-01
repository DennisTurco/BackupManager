package backupmanager.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionDatabaseInitializer extends DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ProductionDatabaseInitializer.class);

    private static final String DEMO_MARKER = ".demo-init";

    // All incremental migrations in version order.
    // Each SQL file must end with: INSERT OR IGNORE INTO SchemaVersion VALUES (N);
    private static final TreeMap<Integer, String> MIGRATIONS = new TreeMap<>();
    static {
        MIGRATIONS.put(4, "/db/004_add_missing_index.sql");
        MIGRATIONS.put(5, "/db/005_soft_delete.sql");
        MIGRATIONS.put(6, "/db/006_schema_fixes.sql");
    }

    public static void init() throws Exception {
        Path dbPath = DatabasePaths.getProductionDatabasePath();
        Files.createDirectories(dbPath.getParent());

        boolean isNewDatabase = !Files.exists(dbPath);
        Path demoMarker = dbPath.getParent().resolve(DEMO_MARKER);
        boolean isDemoInstall = Files.exists(demoMarker);

        try (Connection conn = createConnection(dbPath)) {

            if (isNewDatabase) {
                logger.info("Creating database: {}", dbPath);

                conn.setAutoCommit(false);
                try {
                    runSql(conn, "/db/001_schema.sql");
                    runSql(conn, "/db/002_seed.sql");

                    if (isDemoInstall) {
                        logger.info("Demo marker detected — applying demo configuration");
                        runSql(conn, "/db/003_enable_demo_version.sql");
                    }

                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }

                if (isDemoInstall) {
                    try {
                        Files.delete(demoMarker);
                    } catch (Exception e) {
                        logger.warn("Could not delete demo marker file: {}", demoMarker, e);
                    }
                }

            } else {
                logger.info("Database already exists: {}", dbPath);
                applyPendingMigrations(conn);
            }
        }
    }

    private static void applyPendingMigrations(Connection conn) throws Exception {
        int currentVersion;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(Version), 0) FROM SchemaVersion")) {
            currentVersion = rs.next() ? rs.getInt(1) : 0;
        }
        logger.info("Current schema version: {}", currentVersion);

        for (var entry : MIGRATIONS.entrySet()) {
            if (entry.getKey() > currentVersion) {
                logger.info("Applying migration {}: {}", entry.getKey(), entry.getValue());
                runSql(conn, entry.getValue());
                logger.info("Migration {} applied successfully", entry.getKey());
            }
        }
    }
}
