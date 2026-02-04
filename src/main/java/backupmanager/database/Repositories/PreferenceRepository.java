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

public class PreferenceRepository {

private static final Logger logger = LoggerFactory.getLogger(PreferenceRepository.class);

    public static String getPreferenceValueByCode(String code) {
        String sql = "SELECT Code, Value FROM Preferences WHERE Code = ?";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Value");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching preference with code " + code + ": " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    public static void updatePreferenceValueByCode(String code, String value) {
        String sql = "UPDATE Preferences SET Value = ? WHERE Code = ?";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, code);
            stmt.executeUpdate();

            logger.info("Preference {} updated succesfully with value {}", code, value);

        } catch (SQLException e) {
            logger.error("Preference updating error: " + e.getMessage());
        }
    }
}
