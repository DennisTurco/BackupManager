package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Email;
import backupmanager.Enums.EmailType;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Database;

public class EmailRepository {

    private static final Logger logger = LoggerFactory.getLogger(EmailRepository.class);

    public static void insertEmail(Email email) {
        String sql = "INSERT INTO Emails (Type, InsertDate, AppVersion, Payload) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, email.type().getCode());
            stmt.setLong(2, SqlHelper.toMilliseconds(email.insertDate()));
            stmt.setString(3, email.appVersion());
            stmt.setString(4, email.payload());
            stmt.executeUpdate();

            logger.info("Email inserted succesfully");

        } catch (SQLException e) {
            logger.error("Email inserting error: " + e.getMessage());
        }
    }

    public static Email getLastEmailByType(EmailType type) {
        String sql = """
        SELECT
            EmailId,
            Type,
            InsertDate,
            AppVersion,
            Payload
        FROM
            Emails
        WHERE
            Type = ?
        ORDER BY InsertDate DESC
            """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, type.getCode());

            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    int emailId = rs.getInt("EmailId");
                    long insertDateLong = rs.getLong("InsertDate");
                    String appVersion = rs.getString("AppVersion");
                    String payload = rs.getString("Payload");

                    LocalDateTime startedDate = SqlHelper.toLocalDateTime(insertDateLong);

                    return new Email(emailId, type, startedDate, appVersion, payload);
                } else {
                    logger.debug("No email to obtain");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch last email by type", e);
        }

        return null;
    }

    public static Email getLastErrorEmailByPayloadAndVersion(String payload, String version) {
        String sql = """
        SELECT
            EmailId,
            Type,
            InsertDate,
            AppVersion,
            Payload
        FROM
            Emails
        WHERE
            Type = 2 -- critical error
            AND Payload = ?
            AND AppVersion = ?
        ORDER BY InsertDate DESC
            """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, payload);
            stmt.setString(2, version);

            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    int emailId = rs.getInt("EmailId");
                    int typeInt = rs.getInt("Type");
                    long insertDateLong = rs.getLong("InsertDate");

                    LocalDateTime startedDate = SqlHelper.toLocalDateTime(insertDateLong);
                    EmailType type = EmailType.fromCode(typeInt);

                    return new Email(emailId, type, startedDate, version, payload);
                } else {
                    logger.debug("No email to obtain");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch last email by version and payload", e);
        }

        return null;
    }
}
