package backupmanager.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static String url;

    public static void init(Path dbPath) {
        url = "jdbc:sqlite:" + dbPath;
    }

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(url);

            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }

            return conn;

        } catch (SQLException e) {
            logger.error("Cannot open database connection", e);
            throw new IllegalStateException("Cannot open database connection", e);
        }
    }
}
