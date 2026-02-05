package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Email;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Database;

public class EmailRepository {

    private static final Logger logger = LoggerFactory.getLogger(EmailRepository.class);

    public static void insertEmail(Email email) {
        String sql = "INSERT INTO Emails (Type, InsertDate, AppVersion, Payload) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, email.type());
            stmt.setLong(2, SqlHelper.toMilliseconds(email.insertDate()));
            stmt.setString(3, email.appVersion());
            stmt.setString(4, email.payload());
            stmt.executeUpdate();

            logger.info("Email inserted succesfully");

        } catch (SQLException e) {
            logger.error("Email inserting error: " + e.getMessage());
        }
    }
}
