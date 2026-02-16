package backupmanager.database;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() throws Exception {}

    protected static void runSql(Connection conn, String resource) throws Exception {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream(resource)) {
            if (is == null) return;

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] statements = sql.split(";"); // split for every command

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

    protected static Connection createConnection(Path dbPath) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA temp_store=MEMORY;");
            st.execute("PRAGMA foreign_keys=ON;");
        }

        return conn;
    }
}
