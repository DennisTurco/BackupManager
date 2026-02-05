package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Database;

public class ConfigurationRepository {

private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);

    public static String getConfigurationValueByCode(String code) {
        String sql = "SELECT Code, Value FROM Configurations WHERE Code = ?";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Value");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching configuration with code " + code + ": " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    public static void updateConfigurationValueByCode(String code, String value) {
        String sql = "UPDATE Configurations SET Value = ? WHERE Code = ?";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, code);
            stmt.executeUpdate();

            logger.info("Configuration {} updated succesfully with value {}", code, value);

        } catch (SQLException e) {
            logger.error("Configuration updating error: " + e.getMessage());
        }
    }
}
