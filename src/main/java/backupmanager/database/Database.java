package backupmanager.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Managers.ExceptionManager;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static Path dbPath;

    public static void init(Path path) { dbPath = path; }

    public static Connection getConnection() {
        Connection c;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement st = c.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            } catch (SQLException e) {
                logger.error("Error during the PRAGMA foreign_keys = ON operation: " + e.getMessage());
            }
            return c;
        } catch (SQLException e) {
            logger.error("Error during the connection enstablishing: " + e.getMessage());
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return null;
    }
}
