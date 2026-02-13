package backupmanager.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionDatabaseInitializer extends DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ProductionDatabaseInitializer.class);

    public static void init() throws Exception {
        Path dbPath = DatabasePaths.getProductionDatabasePath();
        Files.createDirectories(dbPath.getParent());

        boolean isNewDatabase = !Files.exists(dbPath);

        try (Connection conn = createConnection(dbPath)) {

            if (isNewDatabase) {
                logger.info("Creating database: {}", dbPath);

                conn.setAutoCommit(false);

                runSql(conn, "/db/001_schema.sql");
                runSql(conn, "/db/002_seed.sql");
                // runSql(conn, "/db/003_enable_demo_version.sql"); Enable it only if you want to create a demo version of the program

                conn.commit();
            } else {
                logger.info("Database already exists: {}", dbPath);
            }
        }
    }
}
