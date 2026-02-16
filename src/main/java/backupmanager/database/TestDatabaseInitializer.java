package backupmanager.database;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

public class TestDatabaseInitializer extends DatabaseInitializer  {

    public static void init() throws Exception {
        Path dbPath = DatabasePaths.getTestDatabasePath();
        Files.createDirectories(dbPath.getParent());

        try (Connection conn = createConnection(dbPath)) {

            conn.setAutoCommit(false);

            runSql(conn, "/db/001_schema.sql");
            runSql(conn, "/db/002_seed.sql");
            runSql(conn, "/db/003_enable_demo_version.sql");

            conn.commit();
        }
    }

    public static void deleteDatabase() throws IOException {

        Path dbPath = DatabasePaths.getTestDatabasePath();
        Path folder = dbPath.getParent();
        String prefix = dbPath.getFileName().toString();

        if (Files.exists(folder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, prefix + "*")) {
                for (Path file : stream)
                    Files.deleteIfExists(file);
            }
        }
    }
}
